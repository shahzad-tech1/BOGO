-- ============================================================
-- BOGO Schema Extension Script
-- Run ONCE against the [bogo] database on DESKTOP-M0688UR\SQLEXPRESS
-- This script is IDEMPOTENT — safe to run multiple times.
-- ============================================================

USE bogo;
GO

-- ============================================================
-- 1. DRIVER — Add assignment and routing columns
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='DRIVER' AND COLUMN_NAME='AssignedRouteId')
    ALTER TABLE DRIVER ADD AssignedRouteId INT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='DRIVER' AND COLUMN_NAME='AssignedBusId')
    ALTER TABLE DRIVER ADD AssignedBusId INT NULL;
GO

-- ============================================================
-- 2. BUS — Add GPS location and health metric columns
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='BUS' AND COLUMN_NAME='CurrentLatitude')
    ALTER TABLE BUS ADD CurrentLatitude FLOAT NULL DEFAULT 0.0;
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='BUS' AND COLUMN_NAME='CurrentLongitude')
    ALTER TABLE BUS ADD CurrentLongitude FLOAT NULL DEFAULT 0.0;
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='BUS' AND COLUMN_NAME='TyreHealth')
    ALTER TABLE BUS ADD TyreHealth FLOAT NULL DEFAULT 100.0;
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='BUS' AND COLUMN_NAME='EngineHealth')
    ALTER TABLE BUS ADD EngineHealth FLOAT NULL DEFAULT 100.0;
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='BUS' AND COLUMN_NAME='ChassisHealth')
    ALTER TABLE BUS ADD ChassisHealth FLOAT NULL DEFAULT 100.0;
GO

-- ============================================================
-- 3. TRIP — Add simulation progress columns
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='TRIP' AND COLUMN_NAME='CurrentStopIndex')
    ALTER TABLE TRIP ADD CurrentStopIndex INT NULL DEFAULT 0;
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='TRIP' AND COLUMN_NAME='SimulatedProgress')
    ALTER TABLE TRIP ADD SimulatedProgress FLOAT NULL DEFAULT 0.0;
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='TRIP' AND COLUMN_NAME='TripStatus')
    ALTER TABLE TRIP ADD TripStatus VARCHAR(20) NULL DEFAULT 'IN_PROGRESS';
GO

-- ============================================================
-- 4. STOPS — Add active flag
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='STOPS' AND COLUMN_NAME='IsActive')
    ALTER TABLE STOPS ADD IsActive BIT NOT NULL DEFAULT 1;
GO

-- ============================================================
-- 5. ROUTEE — Add name and timing columns
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='ROUTEE' AND COLUMN_NAME='RouteName')
    ALTER TABLE ROUTEE ADD RouteName VARCHAR(150) NULL;
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='ROUTEE' AND COLUMN_NAME='EstimatedTimePerStop')
    ALTER TABLE ROUTEE ADD EstimatedTimePerStop INT NOT NULL DEFAULT 120;
GO

-- ============================================================
-- 6. BOOKING — Add PathId foreign key
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='BOOKING' AND COLUMN_NAME='PathId')
    ALTER TABLE BOOKING ADD PathId INT NULL;
GO

-- ============================================================
-- 7. NEW TABLE: MAP (singleton metadata record)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='MAP')
BEGIN
    CREATE TABLE MAP (
        MapId        INT           NOT NULL DEFAULT 1,
        LastRefreshed DATETIME     NULL,
        TotalStops   INT           NULL DEFAULT 0,
        TotalRoutes  INT           NULL DEFAULT 0,
        CONSTRAINT PK_MAP PRIMARY KEY (MapId)
    );
    -- Insert the single metadata row
    INSERT INTO MAP (MapId, LastRefreshed, TotalStops, TotalRoutes)
    VALUES (1, GETDATE(), 0, 0);
END
GO

-- ============================================================
-- 8. NEW TABLE: PATHS (computed passenger journey paths)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='PATHS')
BEGIN
    CREATE TABLE PATHS (
        PathId               INT IDENTITY(1,1) NOT NULL,
        OriginStopId         INT NOT NULL,
        DestinationStopId    INT NOT NULL,
        TotalEstimatedTime   INT NULL DEFAULT 0,
        CreatedAt            DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_PATHS PRIMARY KEY (PathId)
    );
END
GO

-- ============================================================
-- 9. NEW TABLE: PATH_STOPS (ordered stops within a path)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='PATH_STOPS')
BEGIN
    CREATE TABLE PATH_STOPS (
        PathStopId  INT IDENTITY(1,1) NOT NULL,
        PathId      INT NOT NULL,
        StopId      INT NOT NULL,
        Position    INT NOT NULL,
        CONSTRAINT PK_PATH_STOPS PRIMARY KEY (PathStopId),
        CONSTRAINT FK_PATH_STOPS_PATH FOREIGN KEY (PathId) REFERENCES PATHS(PathId)
    );
END
GO

-- ============================================================
-- 10. Update existing TRIP rows to have TripStatus set
-- ============================================================
UPDATE TRIP SET TripStatus = 'IN_PROGRESS' WHERE TripStatus IS NULL;
GO

PRINT 'BOGO schema extension applied successfully.';
GO

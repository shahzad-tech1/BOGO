-- ============================================================
-- BOGO Schema Extension Script
-- Run this ONCE on top of the existing database schema.
-- It extends existing tables and creates new ones.
-- Safe to run: uses IF NOT EXISTS / TRY-CATCH blocks.
-- ============================================================

USE bogo;
GO

-- ============================================================
-- EXTEND DRIVER TABLE
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('DRIVER') AND name = 'AssignedRouteId')
    ALTER TABLE DRIVER ADD AssignedRouteId INT NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('DRIVER') AND name = 'AssignedBusId')
    ALTER TABLE DRIVER ADD AssignedBusId INT NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_DRIVER_ROUTE')
    ALTER TABLE DRIVER ADD CONSTRAINT FK_DRIVER_ROUTE
        FOREIGN KEY (AssignedRouteId) REFERENCES ROUTEE(RouteId);
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_DRIVER_BUS')
    ALTER TABLE DRIVER ADD CONSTRAINT FK_DRIVER_BUS
        FOREIGN KEY (AssignedBusId) REFERENCES BUS(BusId);
GO

-- ============================================================
-- EXTEND BUS TABLE — health metrics and real-time location
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('BUS') AND name = 'CurrentLatitude')
    ALTER TABLE BUS ADD CurrentLatitude FLOAT NULL DEFAULT 0.0;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('BUS') AND name = 'CurrentLongitude')
    ALTER TABLE BUS ADD CurrentLongitude FLOAT NULL DEFAULT 0.0;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('BUS') AND name = 'TyreHealth')
    ALTER TABLE BUS ADD TyreHealth FLOAT NULL DEFAULT 100.0;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('BUS') AND name = 'EngineHealth')
    ALTER TABLE BUS ADD EngineHealth FLOAT NULL DEFAULT 100.0;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('BUS') AND name = 'ChassisHealth')
    ALTER TABLE BUS ADD ChassisHealth FLOAT NULL DEFAULT 100.0;
GO

-- ============================================================
-- EXTEND TRIP TABLE — simulation fields
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('TRIP') AND name = 'CurrentStopIndex')
    ALTER TABLE TRIP ADD CurrentStopIndex INT NULL DEFAULT 0;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('TRIP') AND name = 'SimulatedProgress')
    ALTER TABLE TRIP ADD SimulatedProgress FLOAT NULL DEFAULT 0.0;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('TRIP') AND name = 'TripStatus')
    ALTER TABLE TRIP ADD TripStatus VARCHAR(20) NULL DEFAULT 'IN_PROGRESS';
GO

-- ============================================================
-- EXTEND STOPS TABLE — active flag
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('STOPS') AND name = 'IsActive')
    ALTER TABLE STOPS ADD IsActive BIT NOT NULL DEFAULT 1;
GO

-- ============================================================
-- EXTEND ROUTEE TABLE — route name and timing
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('ROUTEE') AND name = 'RouteName')
    ALTER TABLE ROUTEE ADD RouteName VARCHAR(150) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('ROUTEE') AND name = 'EstimatedTimePerStop')
    ALTER TABLE ROUTEE ADD EstimatedTimePerStop INT NOT NULL DEFAULT 120;
GO

-- ============================================================
-- NEW TABLE: MAP (singleton metadata record)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'MAP') AND type = 'U')
BEGIN
    CREATE TABLE MAP (
        MapId          INT PRIMARY KEY IDENTITY(1,1),
        MapName        VARCHAR(100) NOT NULL DEFAULT 'BOGO_MAIN_MAP',
        LastRefreshed  DATETIME DEFAULT GETDATE(),
        TotalStops     INT DEFAULT 0,
        TotalRoutes    INT DEFAULT 0
    );
    INSERT INTO MAP (MapName) VALUES ('BOGO_MAIN_MAP');
END
GO

-- ============================================================
-- NEW TABLE: PATHS (computed paths for passenger bookings)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'PATHS') AND type = 'U')
BEGIN
    CREATE TABLE PATHS (
        PathId               INT PRIMARY KEY IDENTITY(1,1),
        OriginStopId         INT NOT NULL,
        DestinationStopId    INT NOT NULL,
        TotalEstimatedTime   INT NULL,
        CONSTRAINT FK_PATH_ORIGIN FOREIGN KEY (OriginStopId) REFERENCES STOPS(StopId),
        CONSTRAINT FK_PATH_DEST   FOREIGN KEY (DestinationStopId) REFERENCES STOPS(StopId)
    );
END
GO

-- ============================================================
-- NEW TABLE: PATH_STOPS (ordered stops within a path)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'PATH_STOPS') AND type = 'U')
BEGIN
    CREATE TABLE PATH_STOPS (
        PathId   INT NOT NULL,
        StopId   INT NOT NULL,
        Position INT NOT NULL,
        RouteId  INT NULL,
        PRIMARY KEY (PathId, StopId),
        CONSTRAINT FK_PS_PATH  FOREIGN KEY (PathId)  REFERENCES PATHS(PathId),
        CONSTRAINT FK_PS_STOP  FOREIGN KEY (StopId)  REFERENCES STOPS(StopId),
        CONSTRAINT FK_PS_ROUTE FOREIGN KEY (RouteId) REFERENCES ROUTEE(RouteId)
    );
END
GO

-- ============================================================
-- ADD PathId TO BOOKING (if not exists)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('BOOKING') AND name = 'PathId')
    ALTER TABLE BOOKING ADD PathId INT NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_BOOKING_PATH')
    ALTER TABLE BOOKING ADD CONSTRAINT FK_BOOKING_PATH
        FOREIGN KEY (PathId) REFERENCES PATHS(PathId);
GO

-- ============================================================
-- SEED DATA: stops, routes, buses, trips (only if empty)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM STOPS)
BEGIN
    -- Insert locations first
    INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0479, 33.5651); -- Stop 1: Faizabad
    INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0550, 33.5700); -- Stop 2: Chandni Chowk
    INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0620, 33.5750); -- Stop 3: Saddar
    INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0690, 33.5800); -- Stop 4: Liaquat Bagh
    INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (73.0760, 33.5850); -- Stop 5: Pir Wadhai

    -- Insert stops (Connections are serialized comma-separated StopIds)
    INSERT INTO STOPS (StopName, LocationId, Connections, IsActive)
        VALUES ('Faizabad',      1, '2',   1);
    INSERT INTO STOPS (StopName, LocationId, Connections, IsActive)
        VALUES ('Chandni Chowk', 2, '1,3', 1);
    INSERT INTO STOPS (StopName, LocationId, Connections, IsActive)
        VALUES ('Saddar',        3, '2,4', 1);
    INSERT INTO STOPS (StopName, LocationId, Connections, IsActive)
        VALUES ('Liaquat Bagh',  4, '3,5', 1);
    INSERT INTO STOPS (StopName, LocationId, Connections, IsActive)
        VALUES ('Pir Wadhai',    5, '4',   1);
END
GO

IF NOT EXISTS (SELECT 1 FROM ROUTEE)
BEGIN
    INSERT INTO ROUTEE (Active, Stop_IDs, RouteName, EstimatedTimePerStop)
        VALUES (1, '1,2,3', 'Route A - Faizabad to Saddar', 120);
    INSERT INTO ROUTEE (Active, Stop_IDs, RouteName, EstimatedTimePerStop)
        VALUES (1, '3,4,5', 'Route B - Saddar to Pir Wadhai', 120);
END
GO

IF NOT EXISTS (SELECT 1 FROM BUS)
BEGIN
    INSERT INTO BUS (BusCompany, Registration, BusStatus, Capacity, TyreHealth, EngineHealth, ChassisHealth)
        VALUES ('BOGO Transit', 'LHR-001', 'ACTIVE', 50, 100.0, 100.0, 100.0);
    INSERT INTO BUS (BusCompany, Registration, BusStatus, Capacity, TyreHealth, EngineHealth, ChassisHealth)
        VALUES ('BOGO Transit', 'LHR-002', 'ACTIVE', 50, 100.0, 100.0, 100.0);
END
GO

-- Assign route+bus to the seed driver (UserId = 3 by default — adjust if different)
-- Only update if the driver exists and hasn't been assigned yet
UPDATE DRIVER
    SET AssignedRouteId = 1, AssignedBusId = 1
WHERE UserId = 3
  AND AssignedRouteId IS NULL;
GO

-- Seed active trips for simulation (only if TRIP is empty)
IF NOT EXISTS (SELECT 1 FROM TRIP)
BEGIN
    INSERT INTO TRIP (RouteId, BusId, DriverId, DepartureTime, ArrivalTime,
                      CurrentStopIndex, SimulatedProgress, TripStatus)
        VALUES (1, 1, 3,
                GETDATE(), DATEADD(HOUR, 1, GETDATE()),
                0, 0.0, 'IN_PROGRESS');
    INSERT INTO TRIP (RouteId, BusId, DriverId, DepartureTime, ArrivalTime,
                      CurrentStopIndex, SimulatedProgress, TripStatus)
        VALUES (2, 2, 3,
                GETDATE(), DATEADD(HOUR, 1, GETDATE()),
                0, 0.0, 'IN_PROGRESS');
END
GO

PRINT 'BOGO schema extension complete.';
GO

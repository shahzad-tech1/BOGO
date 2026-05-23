USE BOGO;
-- ============================================================
-- seed_active_trips.sql
-- Run this against the BOGO SQL Server database to ensure
-- at least 2 IN_PROGRESS trips exist for map simulation.
-- Safe to run multiple times (only inserts if none exist).
-- ============================================================

-- Step 1: Add TripStatus / simulation columns if not yet present
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME='TRIP' AND COLUMN_NAME='TripStatus')
    ALTER TABLE TRIP ADD TripStatus VARCHAR(20) DEFAULT 'IN_PROGRESS';

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME='TRIP' AND COLUMN_NAME='CurrentStopIndex')
    ALTER TABLE TRIP ADD CurrentStopIndex INT DEFAULT 0;

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME='TRIP' AND COLUMN_NAME='SimulatedProgress')
    ALTER TABLE TRIP ADD SimulatedProgress FLOAT DEFAULT 0.0;

-- Step 2: Only insert sample trips if none are IN_PROGRESS
IF NOT EXISTS (SELECT 1 FROM TRIP WHERE TripStatus = 'IN_PROGRESS')
BEGIN
    -- Use the first available RouteId, BusId, and DriverId in the DB
    DECLARE @route1 INT = (SELECT TOP 1 RouteId FROM ROUTEE WHERE Active = 1 ORDER BY RouteId);
    DECLARE @route2 INT = (SELECT TOP 1 RouteId FROM ROUTEE WHERE Active = 1 ORDER BY RouteId DESC);
    DECLARE @bus1   INT = (SELECT TOP 1 BusId FROM BUS ORDER BY BusId);
    DECLARE @bus2   INT = (SELECT TOP 1 BusId FROM BUS ORDER BY BusId DESC);
    DECLARE @drv1   INT = (SELECT TOP 1 UserId FROM DRIVER ORDER BY UserId);

    IF @route1 IS NOT NULL AND @bus1 IS NOT NULL AND @drv1 IS NOT NULL
    BEGIN
        INSERT INTO TRIP (RouteId, BusId, DriverId, DepartureTime, ArrivalTime,
                          CurrentStopIndex, SimulatedProgress, TripStatus)
        VALUES
            (@route1, @bus1, @drv1,
             GETDATE(), DATEADD(HOUR, 2, GETDATE()),
             0, 0.0, 'IN_PROGRESS');

        -- Second trip only if we have a distinct route/bus
        IF @route2 <> @route1 OR @bus2 <> @bus1
        BEGIN
            INSERT INTO TRIP (RouteId, BusId, DriverId, DepartureTime, ArrivalTime,
                              CurrentStopIndex, SimulatedProgress, TripStatus)
            VALUES
                (ISNULL(@route2, @route1), ISNULL(@bus2, @bus1), @drv1,
                 GETDATE(), DATEADD(HOUR, 3, GETDATE()),
                 0, 0.0, 'IN_PROGRESS');
        END
        PRINT 'Seeded active trips for simulation.';
    END
    ELSE
        PRINT 'Cannot seed trips — ensure at least one ROUTEE, BUS, and DRIVER exists first.';
END
ELSE
    PRINT 'Active trips already exist — no seeding needed.';

-- Step 3: Confirm what is in the DB
SELECT TripId, RouteId, BusId, DriverId, TripStatus, CurrentStopIndex
FROM TRIP
WHERE TripStatus = 'IN_PROGRESS';

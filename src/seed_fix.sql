-- ============================================================
-- BOGO Seed Fix Script
-- Run this AFTER schema_extension.sql has been applied.
-- This script ONLY fixes the two failed seed sections:
--   1. BUS rows   (failed: 'ACTIVE' vs CHECK constraint)
--   2. TRIP rows  (failed: FK because BUS rows didn't exist)
--
-- Everything else (ALTER TABLE, STOPS, ROUTEE, MAP, PATHS,
-- PATH_STOPS) already succeeded — this script does NOT
-- touch any of those tables.
-- ============================================================

USE bogo;
GO

-- ============================================================
-- SECTION 1: Seed buses (correct BusStatus = 'AVAILABLE')
-- Guard: only runs if BUS table is still empty
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM BUS)
BEGIN
    INSERT INTO BUS (BusCompany, Registration, RegistrationYear, BusStatus, Capacity,
                     TyreHealth, EngineHealth, ChassisHealth)
        VALUES ('BOGO Transit', 'LHR-001', 2022, 'AVAILABLE', 50, 100.0, 100.0, 100.0);

    INSERT INTO BUS (BusCompany, Registration, RegistrationYear, BusStatus, Capacity,
                     TyreHealth, EngineHealth, ChassisHealth)
        VALUES ('BOGO Transit', 'LHR-002', 2023, 'AVAILABLE', 50, 100.0, 100.0, 100.0);

    PRINT 'BUS: 2 rows seeded.';
END
ELSE
    PRINT 'BUS: already has data — skipped.';
GO

-- ============================================================
-- SECTION 2: Assign route+bus to the seed driver
-- Uses dynamic lookup so no hardcoded IDs
-- Guard: only updates rows where AssignedRouteId is still NULL
-- ============================================================
DECLARE @FirstDriverId INT = (SELECT TOP 1 UserId FROM DRIVER ORDER BY UserId);
DECLARE @FirstBusId    INT = (SELECT TOP 1 BusId  FROM BUS   ORDER BY BusId);
DECLARE @FirstRouteId  INT = (SELECT TOP 1 RouteId FROM ROUTEE ORDER BY RouteId);

IF @FirstDriverId IS NOT NULL AND @FirstBusId IS NOT NULL AND @FirstRouteId IS NOT NULL
BEGIN
    UPDATE DRIVER
        SET AssignedRouteId = @FirstRouteId,
            AssignedBusId   = @FirstBusId
    WHERE UserId = @FirstDriverId
      AND AssignedRouteId IS NULL;

    PRINT 'DRIVER: assignment updated.';
END
ELSE
    PRINT 'DRIVER: no driver/bus/route found to assign — skipped.';
GO

-- ============================================================
-- SECTION 3: Seed trips (only if TRIP table is still empty)
-- Uses dynamic IDs — no hardcoded BusId/RouteId/DriverId
-- Guard: only runs if TRIP is empty AND buses now exist
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM TRIP) AND EXISTS (SELECT 1 FROM BUS)
BEGIN
    DECLARE @Bus1   INT = (SELECT TOP 1 BusId   FROM BUS   ORDER BY BusId);
    DECLARE @Bus2   INT = (SELECT TOP 1 BusId   FROM BUS   ORDER BY BusId DESC);
    DECLARE @Route1 INT = (SELECT TOP 1 RouteId FROM ROUTEE ORDER BY RouteId);
    DECLARE @Route2 INT = (SELECT TOP 1 RouteId FROM ROUTEE ORDER BY RouteId DESC);
    DECLARE @Driver INT = (SELECT TOP 1 UserId  FROM DRIVER ORDER BY UserId);

    IF @Bus1 IS NOT NULL AND @Route1 IS NOT NULL AND @Driver IS NOT NULL
    BEGIN
        -- Trip 1: first bus on first route
        INSERT INTO TRIP (RouteId, BusId, DriverId, DepartureTime, ArrivalTime,
                          CurrentStopIndex, SimulatedProgress, TripStatus)
            VALUES (@Route1, @Bus1, @Driver,
                    GETDATE(), DATEADD(HOUR, 1, GETDATE()),
                    0, 0.0, 'IN_PROGRESS');

        -- Trip 2: second bus on second route (only if they are different)
        IF @Bus2 <> @Bus1 AND @Route2 <> @Route1
        BEGIN
            INSERT INTO TRIP (RouteId, BusId, DriverId, DepartureTime, ArrivalTime,
                              CurrentStopIndex, SimulatedProgress, TripStatus)
                VALUES (@Route2, @Bus2, @Driver,
                        GETDATE(), DATEADD(HOUR, 1, GETDATE()),
                        0, 0.0, 'IN_PROGRESS');
        END

        PRINT 'TRIP: seeded successfully.';
    END
    ELSE
        PRINT 'TRIP: missing prerequisite data — skipped.';
END
ELSE IF EXISTS (SELECT 1 FROM TRIP)
    PRINT 'TRIP: already has data — skipped.';
ELSE
    PRINT 'TRIP: no buses exist yet — run after BUS is seeded.';
GO

-- Patch any existing TRIP rows that have NULL TripStatus
-- (from rows that existed before the column was added)
UPDATE TRIP SET TripStatus = 'IN_PROGRESS' WHERE TripStatus IS NULL;
GO

PRINT 'Seed fix complete — no conflicts with previously run statements.';
GO

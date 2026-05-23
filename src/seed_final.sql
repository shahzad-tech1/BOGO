-- ============================================================
-- BOGO Final Seed Script  (seed_final.sql)
-- Run this AFTER schema.sql + Schema_Extention_FIle.sql +
--   schema_extension.sql have been applied.
--
-- What this script does:
--   1. Diagnoses and fixes the BusStatus CHECK constraint on BUS
--      so that 'ACTIVE', 'DOWN', and 'MAINTENANCE' are accepted.
--   2. Seeds BUS rows (guard: skipped if BUS already has data).
--   3. Assigns first route + bus to the seed driver (guard: only
--      if not already assigned).
--   4. Seeds TRIP rows (guard: skipped if TRIP already has data).
--   5. Patches any NULL TripStatus rows in TRIP.
--
-- Safe to re-run: every section is guarded with IF NOT EXISTS
-- or UPDATE ... WHERE <condition>.  No duplicate key errors.
-- ============================================================

USE bogo;
GO

-- ============================================================
-- SECTION 0: Fix the BusStatus CHECK constraint
-- The existing auto-generated constraint rejected both 'ACTIVE'
-- and 'AVAILABLE'.  Drop it and replace it with an explicit
-- constraint that covers all statuses the app uses.
-- ============================================================
IF EXISTS (
    SELECT 1
    FROM sys.check_constraints cc
    JOIN sys.objects  o ON o.object_id = cc.parent_object_id
    WHERE o.name = 'BUS'
      AND cc.name LIKE '%BusStatus%'
)
BEGIN
    DECLARE @ConstraintName NVARCHAR(200);

    SELECT TOP 1 @ConstraintName = cc.name
    FROM sys.check_constraints cc
    JOIN sys.objects o ON o.object_id = cc.parent_object_id
    WHERE o.name = 'BUS'
      AND cc.name LIKE '%BusStatus%';

    EXEC ('ALTER TABLE BUS DROP CONSTRAINT [' + @ConstraintName + ']');

    PRINT 'BUS CHECK constraint dropped: ' + @ConstraintName;
END
ELSE
    PRINT 'BUS: no BusStatus CHECK constraint found — nothing to drop.';
GO

-- Re-add the constraint with all valid statuses the application uses:
--   ACTIVE       — bus is in service (domain / technical doc)
--   DOWN         — bus reported broken
--   MAINTENANCE  — bus under maintenance
--   AVAILABLE    — bus available but not yet assigned to a trip
--   INACTIVE     — bus temporarily out of service
IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints cc
    JOIN sys.objects o ON o.object_id = cc.parent_object_id
    WHERE o.name = 'BUS'
      AND cc.name = 'CK_BUS_BusStatus'
)
BEGIN
    ALTER TABLE BUS
        ADD CONSTRAINT CK_BUS_BusStatus
        CHECK (BusStatus IN ('ACTIVE', 'DOWN', 'MAINTENANCE', 'AVAILABLE', 'INACTIVE'));

    PRINT 'BUS: new CK_BUS_BusStatus constraint added.';
END
ELSE
    PRINT 'BUS: CK_BUS_BusStatus already exists — skipped.';
GO

-- ============================================================
-- SECTION 1: Seed BUS rows
-- Guard: only runs if BUS table is completely empty.
-- Uses BusStatus = 'ACTIVE' (matches domain model).
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM BUS)
BEGIN
    INSERT INTO BUS (BusCompany, Registration, RegistrationYear, BusStatus, Capacity,
                     TyreHealth, EngineHealth, ChassisHealth)
        VALUES ('BOGO Transit', 'LHR-001', 2022, 'ACTIVE', 50, 100.0, 100.0, 100.0);

    INSERT INTO BUS (BusCompany, Registration, RegistrationYear, BusStatus, Capacity,
                     TyreHealth, EngineHealth, ChassisHealth)
        VALUES ('BOGO Transit', 'LHR-002', 2023, 'ACTIVE', 50, 100.0, 100.0, 100.0);

    PRINT 'BUS: 2 rows seeded.';
END
ELSE
    PRINT 'BUS: already has data — skipped.';
GO

-- ============================================================
-- SECTION 2: Assign route + bus to the seed driver
-- Uses dynamic lookup (no hardcoded IDs).
-- Guard: only updates rows where AssignedRouteId is NULL.
-- ============================================================
DECLARE @FirstDriverId  INT = (SELECT TOP 1 UserId  FROM DRIVER  ORDER BY UserId);
DECLARE @FirstBusId     INT = (SELECT TOP 1 BusId   FROM BUS     ORDER BY BusId);
DECLARE @FirstRouteId   INT = (SELECT TOP 1 RouteId FROM ROUTEE  ORDER BY RouteId);

IF @FirstDriverId IS NOT NULL AND @FirstBusId IS NOT NULL AND @FirstRouteId IS NOT NULL
BEGIN
    UPDATE DRIVER
        SET AssignedRouteId = @FirstRouteId,
            AssignedBusId   = @FirstBusId
    WHERE UserId = @FirstDriverId
      AND AssignedRouteId IS NULL;

    PRINT 'DRIVER: assignment updated (or already assigned — no-op).';
END
ELSE
    PRINT 'DRIVER: no driver / bus / route found to assign — skipped.';
GO

-- ============================================================
-- SECTION 3: Seed TRIP rows
-- Guard: only runs if TRIP is empty AND buses now exist.
-- Uses dynamic IDs — no hardcoded values.
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

        -- Trip 2: second bus on second route (only if they differ)
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
    PRINT 'TRIP: BUS table is still empty — seed BUS first.';
GO

-- ============================================================
-- SECTION 4: Patch any existing TRIP rows with NULL TripStatus
-- (Safety net for rows inserted before the column was added)
-- ============================================================
UPDATE TRIP
    SET TripStatus = 'IN_PROGRESS'
WHERE TripStatus IS NULL;
GO

PRINT 'Seed final complete — all sections processed.';
GO

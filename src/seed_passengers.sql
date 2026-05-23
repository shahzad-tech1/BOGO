USE bogo;
GO

-- ================================================================
-- seed_passengers.sql
-- Seeds 200 passengers (10 per bus × 20 buses) + 200 bookings.
-- All tables touched: PERSONAL_DETAILS, USERS, PASSENGER, BOOKING
--
-- PdId / UserId range : 2001 – 2200  (drivers occupy 1001 – 1020)
-- Bus mapping         : Bus 1001 → passengers 2001-2010
--                       Bus 1002 → passengers 2011-2020  … etc.
--
-- Constraints observed:
--   Email    : LIKE '%@%'   (relaxed by fix_constraints.sql)
--   Password : Bogo1234     (8-20 chars, 1 uppercase, 1 digit)
--   CNIC     : 13 digits    (no dashes)
--
-- Wrapped in BEGIN TRANSACTION / CATCH ROLLBACK — fully atomic.
-- Safe to run repeatedly: guard clause aborts if already seeded.
-- ================================================================

BEGIN TRANSACTION;

BEGIN TRY

    PRINT 'Starting passenger + booking seed (200 passengers, 20 buses × 10 each)...';

    -- ── Guard: skip if already seeded ─────────────────────────────
    IF EXISTS (SELECT 1 FROM PERSONAL_DETAILS WHERE PdId >= 2001)
    BEGIN
        PRINT 'Passengers (PdId >= 2001) already exist — nothing inserted.';
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- ================================================================
    -- SECTION 1 : PERSONAL_DETAILS  (200 rows, PdId 2001-2200)
    -- ================================================================
    SET IDENTITY_INSERT PERSONAL_DETAILS ON;

    DECLARE @bus   INT,
            @p     INT,
            @pdId  INT,
            @name  NVARCHAR(100),
            @email NVARCHAR(100),
            @cnic  CHAR(13);

    SET @bus  = 1;
    SET @pdId = 2001;

    WHILE @bus <= 20
    BEGIN
        SET @p = 1;
        WHILE @p <= 10
        BEGIN
            SET @name  = 'Passenger B'
                         + RIGHT('00' + CAST(@bus  AS VARCHAR(3)), 2)
                         + '-P'
                         + RIGHT('0'  + CAST(@p    AS VARCHAR(2)), 2);

            SET @email = 'b'
                         + RIGHT('00' + CAST(@bus  AS VARCHAR(3)), 2)
                         + 'p'
                         + RIGHT('0'  + CAST(@p    AS VARCHAR(2)), 2)
                         + '@gmail.com';

            -- 13-digit CNIC: zero-pad sequential number
            SET @cnic = RIGHT('0000000000000' + CAST(@pdId AS VARCHAR(5)), 13);

            INSERT INTO PERSONAL_DETAILS (PdId, Name, Email, Password, CNIC)
            VALUES (@pdId, @name, @email, 'Bogo1234', @cnic);

            SET @pdId = @pdId + 1;
            SET @p    = @p    + 1;
        END
        SET @bus = @bus + 1;
    END

    SET IDENTITY_INSERT PERSONAL_DETAILS OFF;
    PRINT '  ✔ PERSONAL_DETAILS: 200 rows inserted (PdId 2001-2200).';

    -- ================================================================
    -- SECTION 2 : USERS  (200 rows, UserId 2001-2200)
    -- ================================================================
    SET IDENTITY_INSERT USERS ON;

    DECLARE @uid INT = 2001;
    WHILE @uid <= 2200
    BEGIN
        INSERT INTO USERS (UserId, PdId) VALUES (@uid, @uid);
        SET @uid = @uid + 1;
    END

    SET IDENTITY_INSERT USERS OFF;
    PRINT '  ✔ USERS: 200 rows inserted (UserId 2001-2200).';

    -- ================================================================
    -- SECTION 3 : PASSENGER  (200 rows — one per user above)
    -- ================================================================
    DECLARE @puid INT = 2001;
    WHILE @puid <= 2200
    BEGIN
        INSERT INTO PASSENGER (UserId) VALUES (@puid);
        SET @puid = @puid + 1;
    END

    PRINT '  ✔ PASSENGER: 200 rows inserted.';

    -- ================================================================
    -- SECTION 4 : BOOKING  (200 rows)
    -- Passenger UserId 2001-2010  → BusId 1001  (Route 1)
    -- Passenger UserId 2011-2020  → BusId 1002  (Route 2)
    --   …
    -- Passenger UserId 2191-2200  → BusId 1020  (Route 20)
    --
    -- Cost    : 150 PKR (flat fare)
    -- Active  : 1 (live booking)
    -- ================================================================
    DECLARE @pasId  INT = 2001,
            @busId  INT = 1001,
            @bookP  INT;

    SET @bus = 1;
    WHILE @bus <= 20
    BEGIN
        SET @bookP = 1;
        WHILE @bookP <= 10
        BEGIN
            INSERT INTO BOOKING
                (PassengerID, BusID, Active, Cost, BookingTime, PathDescription)
            VALUES
                (@pasId,
                 @busId,
                 1,          -- Active
                 150.00,     -- Cost (PKR)
                 GETDATE(),
                 'Bus ' + CAST(@busId AS VARCHAR(4))
                 + ' | Route ' + CAST(@bus AS VARCHAR(3))
                 + ' | Seat ' + CAST(@bookP AS VARCHAR(2)));

            SET @pasId  = @pasId  + 1;
            SET @bookP  = @bookP  + 1;
        END
        SET @busId = @busId + 1;
        SET @bus   = @bus   + 1;
    END

    PRINT '  ✔ BOOKING: 200 rows inserted (10 per bus, buses 1001-1020).';

    -- ================================================================
    -- COMMIT
    -- ================================================================
    COMMIT TRANSACTION;
    PRINT '';
    PRINT '=== SUCCESS: 200 passengers + 200 bookings committed. ===';

END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;

    PRINT '';
    PRINT '=== FAILED — entire transaction rolled back. ===';
    PRINT 'Error #'    + CAST(ERROR_NUMBER()    AS VARCHAR) + ': ' + ERROR_MESSAGE();
    PRINT 'Severity: ' + CAST(ERROR_SEVERITY()  AS VARCHAR);
    PRINT 'State: '    + CAST(ERROR_STATE()     AS VARCHAR);
    PRINT 'Line: '     + CAST(ERROR_LINE()      AS VARCHAR);

    -- Re-raise so the calling tool / SSMS also sees the failure
    THROW;
END CATCH;
GO

-- ================================================================
-- VERIFICATION QUERY — run after seed to confirm counts
-- ================================================================
PRINT '=== Post-seed verification ===';
SELECT 'PERSONAL_DETAILS (passengers)' AS [Table],
       COUNT(*) AS [Rows Inserted]
FROM PERSONAL_DETAILS WHERE PdId BETWEEN 2001 AND 2200

UNION ALL
SELECT 'USERS (passengers)',    COUNT(*) FROM USERS     WHERE UserId BETWEEN 2001 AND 2200
UNION ALL
SELECT 'PASSENGER',            COUNT(*) FROM PASSENGER  WHERE UserId BETWEEN 2001 AND 2200
UNION ALL
SELECT 'BOOKING (active, ≥2001)', COUNT(*)
FROM BOOKING WHERE PassengerID BETWEEN 2001 AND 2200 AND Active = 1;
GO

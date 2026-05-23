-- ================================================================
-- fix_constraints.sql  |  BOGO — Relax DB constraints for Passenger
-- Run ONCE against: DESKTOP-M0688UR\SQLEXPRESS  database: bogo
-- ================================================================
USE bogo;
GO

-- Drop the overly-restrictive gmail-only email constraint
IF EXISTS (SELECT 1 FROM sys.check_constraints
           WHERE name = 'CK__PERSONAL___Email__5DCAEF64'
             AND parent_object_id = OBJECT_ID('PERSONAL_DETAILS'))
BEGIN
    ALTER TABLE PERSONAL_DETAILS
        DROP CONSTRAINT CK__PERSONAL___Email__5DCAEF64;
    PRINT 'Dropped gmail email constraint.';
END

-- Add a reasonable email constraint (must contain @)
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name = 'CK_PERSONAL_Email_format'
                 AND parent_object_id = OBJECT_ID('PERSONAL_DETAILS'))
BEGIN
    ALTER TABLE PERSONAL_DETAILS
        ADD CONSTRAINT CK_PERSONAL_Email_format
        CHECK (Email LIKE '%@%');
    PRINT 'Added relaxed email constraint.';
END

-- Make CNIC constraint optional-friendly (allow placeholder '0000000000000')
-- The existing CNIC constraint already enforces 13 digits which is correct.
-- We keep it — just ensure the app sends 13 digits.
PRINT 'CNIC constraint kept as-is (13 digits).';
GO

PRINT '=== Constraint fix complete ===';
GO

/*
   SQL Server Table Creation Script
   Database: bogo
   Description: Bus Booking/Management System
*/

-- 1. PERSONAL_DETAILS (Dependency for USERS)
CREATE TABLE PERSONAL_DETAILS (
    PdId INT PRIMARY KEY IDENTITY(1,1),
    [Name] VARCHAR(30) NOT NULL,
    Email VARCHAR(100) NULL,
    [Password] VARCHAR(50) NULL,
    CNIC VARCHAR(13) NULL
);

-- 2. USERS (Dependency for ADMIN, PASSENGER, DRIVER)
CREATE TABLE USERS (
    UserId INT PRIMARY KEY IDENTITY(1,1),
    PdId INT NOT NULL,
    CONSTRAINT FK_USERS_PERSONAL_DETAILS FOREIGN KEY (PdId) 
        REFERENCES PERSONAL_DETAILS(PdId)
);

-- 3. ADMIN (Specialization of USERS)
CREATE TABLE ADMIN (
    UserId INT PRIMARY KEY,
    CONSTRAINT FK_ADMIN_USERS FOREIGN KEY (UserId) 
        REFERENCES USERS(UserId)
);

-- 4. PASSENGER (Specialization of USERS)
CREATE TABLE PASSENGER (
    UserId INT PRIMARY KEY,
    CONSTRAINT FK_PASSENGER_USERS FOREIGN KEY (UserId) 
        REFERENCES USERS(UserId)
);

-- 5. DRIVER (Specialization of USERS)
CREATE TABLE DRIVER (
    UserId INT PRIMARY KEY,
    DriverID CHAR(13) NOT NULL,
    CONSTRAINT FK_DRIVER_USERS FOREIGN KEY (UserId) 
        REFERENCES USERS(UserId)
);

-- 6. LOCATIONN (Dependency for STOPS)
CREATE TABLE LOCATIONN (
    LocationId INT PRIMARY KEY IDENTITY(1,1),
    Longitude FLOAT NOT NULL,
    Latitude FLOAT NOT NULL
);

-- 7. STOPS
CREATE TABLE STOPS (
    StopId INT PRIMARY KEY IDENTITY(1,1),
    StopName VARCHAR(50) NOT NULL,
    LocationId INT NOT NULL,
    Connections NVARCHAR(MAX) NOT NULL,
    CONSTRAINT FK_STOPS_LOCATIONN FOREIGN KEY (LocationId) 
        REFERENCES LOCATIONN(LocationId)
);

-- 8. ROUTEE
CREATE TABLE ROUTEE (
    RouteId INT PRIMARY KEY IDENTITY(1,1),
    Active BIT NOT NULL,
    Stop_IDs NVARCHAR(MAX) NOT NULL
);

-- 9. BUS
CREATE TABLE BUS (
    BusId INT PRIMARY KEY IDENTITY(1,1),
    BusCompany VARCHAR(50) NOT NULL,
    Registration VARCHAR(7) NULL,
    RegistrationYear INT NULL,
    BusStatus VARCHAR(20) NULL,
    Capacity INT NULL
);

-- 10. TRIP
CREATE TABLE TRIP (
    TripId INT PRIMARY KEY IDENTITY(1,1),
    RouteId INT NOT NULL,
    BusId INT NOT NULL,
    DriverId INT NOT NULL,
    DepartureTime DATETIME NOT NULL,
    ArrivalTime DATETIME NOT NULL,
    CONSTRAINT FK_TRIP_ROUTEE FOREIGN KEY (RouteId) REFERENCES ROUTEE(RouteId),
    CONSTRAINT FK_TRIP_BUS FOREIGN KEY (BusId) REFERENCES BUS(BusId),
    CONSTRAINT FK_TRIP_DRIVER FOREIGN KEY (DriverId) REFERENCES DRIVER(UserId)
);

-- 11. BOOKING
CREATE TABLE BOOKING (
    BookingID INT PRIMARY KEY IDENTITY(1,1),
    PassengerID INT NOT NULL,
    BusID INT NOT NULL,
    Active BIT NOT NULL,
    Cost FLOAT NOT NULL,
    BookingTime DATETIME NOT NULL,
    CONSTRAINT FK_BOOKING_PASSENGER FOREIGN KEY (PassengerID) REFERENCES PASSENGER(UserId),
    CONSTRAINT FK_BOOKING_BUS FOREIGN KEY (BusID) REFERENCES BUS(BusId)
);

CREATE TABLE ALERTS (
                        AlertId INT PRIMARY KEY IDENTITY(1,1),
                        SenderDriverId INT NOT NULL,
                        AlertType VARCHAR(20) NOT NULL, -- 'BUS_DOWN', 'DRIVER_DOWN', 'ROUTE_BLOCKAGE'
                        Priority VARCHAR(10) NOT NULL,  -- 'HIGH', 'LOW'
    [Message] NVARCHAR(MAX),
    Status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'RESOLVED'
    SentTime DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_ALERTS_DRIVER FOREIGN KEY (SenderDriverId) REFERENCES DRIVER(UserId)
    );

CREATE TABLE RESOLUTIONS (
                             ResolutionId INT PRIMARY KEY IDENTITY(1,1),
                             AlertId INT NOT NULL,
                             AdminId INT NOT NULL,
                             NewBusId INT NULL,
                             NewDriverId INT NULL,
                             NewRouteId INT NULL,
                             ResolutionNotes NVARCHAR(MAX),
                             ResolvedAt DATETIME DEFAULT GETDATE(),
                             CONSTRAINT FK_RES_ALERT FOREIGN KEY (AlertId) REFERENCES ALERTS(AlertId),
                             CONSTRAINT FK_RES_ADMIN FOREIGN KEY (AdminId) REFERENCES ADMIN(UserId),
                             CONSTRAINT FK_RES_BUS FOREIGN KEY (NewBusId) REFERENCES BUS(BusId),
                             CONSTRAINT FK_RES_DRIVER FOREIGN KEY (NewDriverId) REFERENCES DRIVER(UserId),
                             CONSTRAINT FK_RES_ROUTE FOREIGN KEY (NewRouteId) REFERENCES ROUTEE(RouteId)
);

CREATE TABLE NOTIFICATIONS (
                               NotificationId INT PRIMARY KEY IDENTITY(1,1),
                               RecipientUserId INT NOT NULL,
                               Content NVARCHAR(MAX) NOT NULL,
                               IsRead BIT DEFAULT 0,
                               CreatedAt DATETIME DEFAULT GETDATE(),
                               CONSTRAINT FK_NOTIF_USER FOREIGN KEY (RecipientUserId) REFERENCES USERS(UserId)
);



BEGIN TRANSACTION;

BEGIN TRY
    DECLARE @AdminPdId INT, @AdminUserId INT;

    -- 1. Insert Core Identity
    -- Updated Email to satisfy the '%_@gmail.com' constraint
INSERT INTO PERSONAL_DETAILS ([Name], Email, [Password], CNIC)
VALUES ('Super Admin', 'bogo.admin@gmail.com', 'AdminPass123', '1234500000000');

SET @AdminPdId = SCOPE_IDENTITY();

    -- 2. Insert into System Users table
    IF @AdminPdId IS NOT NULL
BEGIN
INSERT INTO USERS (PdId)
VALUES (@AdminPdId);
SET @AdminUserId = SCOPE_IDENTITY();
END

    -- 3. Assign the Admin Role
    IF @AdminUserId IS NOT NULL
BEGIN
INSERT INTO ADMIN (UserId)
VALUES (@AdminUserId);

COMMIT TRANSACTION;
PRINT 'Admin successfully inserted with Gmail address.';
END
ELSE
BEGIN
ROLLBACK TRANSACTION;
PRINT 'Error: UserId was not generated.';
END

END TRY
BEGIN CATCH
IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT 'Error Message: ' + ERROR_MESSAGE();
END CATCH;


BEGIN TRANSACTION;

BEGIN TRY
    DECLARE @PassengerPdId INT, @PassengerUserId INT;

    -- 1. Insert Core Identity (Satisfies '%_@gmail.com' constraint)
INSERT INTO PERSONAL_DETAILS ([Name], Email, [Password], CNIC)
VALUES ('John Passenger', 'john.bogo@gmail.com', 'SecurePass123', '4210112345678');

SET @PassengerPdId = SCOPE_IDENTITY();

    -- 2. Insert into System Users table
    IF @PassengerPdId IS NOT NULL
BEGIN
INSERT INTO USERS (PdId)
VALUES (@PassengerPdId);
SET @PassengerUserId = SCOPE_IDENTITY();
END

    -- 3. Assign the Passenger Role
    IF @PassengerUserId IS NOT NULL
BEGIN
INSERT INTO PASSENGER (UserId)
VALUES (@PassengerUserId);

COMMIT TRANSACTION;
PRINT 'Passenger successfully inserted.';
END
ELSE
BEGIN
ROLLBACK TRANSACTION;
PRINT 'Error: Passenger UserId was not generated.';
END

END TRY
BEGIN CATCH
IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT 'Error Message: ' + ERROR_MESSAGE();
END CATCH;



BEGIN TRANSACTION;

BEGIN TRY
    DECLARE @DriverPdId INT, @DriverUserId INT;

    -- 1. Insert Core Identity
INSERT INTO PERSONAL_DETAILS ([Name], Email, [Password], CNIC)
VALUES ('Fast Driver', 'driver.bogo@gmail.com', 'DriveSafe2024', '4210198765432');

SET @DriverPdId = SCOPE_IDENTITY();

    -- 2. Insert into System Users table
    IF @DriverPdId IS NOT NULL
BEGIN
INSERT INTO USERS (PdId)
VALUES (@DriverPdId);
SET @DriverUserId = SCOPE_IDENTITY();
END

    -- 3. Assign the Driver Role (Requires the 13-character DriverID)
    IF @DriverUserId IS NOT NULL
BEGIN
INSERT INTO DRIVER (UserId, DriverID)
VALUES (@DriverUserId, 'DRI-5566-2024');

COMMIT TRANSACTION;
PRINT 'Driver successfully inserted.';
END
ELSE
BEGIN
ROLLBACK TRANSACTION;
PRINT 'Error: Driver UserId was not generated.';
END

END TRY
BEGIN CATCH
IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT 'Error Message: ' + ERROR_MESSAGE();
END CATCH;

SELECT
    SCHEMA_NAME(t.schema_id) AS SchemaName,
    t.name AS TableName,
    col.name AS ColumnName,
    chk.name AS ConstraintName,
    'CHECK' AS ConstraintType,
    chk.definition AS ConstraintDefinition
FROM sys.check_constraints chk
         INNER JOIN sys.tables t ON chk.parent_object_id = t.object_id
         LEFT JOIN sys.columns col ON chk.parent_object_id = col.object_id AND chk.parent_column_id = col.column_id
WHERE t.name IN ('PERSONAL_DETAILS', 'USERS', 'ADMIN', 'PASSENGER', 'DRIVER', 'LOCATIONN', 'STOPS', 'ROUTEE', 'BUS', 'TRIP', 'BOOKING', 'ALERTS', 'RESOLUTIONS', 'NOTIFICATIONS')

UNION ALL

SELECT
    SCHEMA_NAME(t.schema_id) AS SchemaName,
    t.name AS TableName,
    col.name AS ColumnName,
    def.name AS ConstraintName,
    'DEFAULT' AS ConstraintType,
    def.definition AS ConstraintDefinition
FROM sys.default_constraints def
         INNER JOIN sys.tables t ON def.parent_object_id = t.object_id
         LEFT JOIN sys.columns col ON def.parent_object_id = col.object_id AND def.parent_column_id = col.column_id
WHERE t.name IN ('PERSONAL_DETAILS', 'USERS', 'ADMIN', 'PASSENGER', 'DRIVER', 'LOCATIONN', 'STOPS', 'ROUTEE', 'BUS', 'TRIP', 'BOOKING', 'ALERTS', 'RESOLUTIONS', 'NOTIFICATIONS')

UNION ALL

SELECT
    SCHEMA_NAME(t.schema_id) AS SchemaName,
    t.name AS TableName,
    col.name AS ColumnName,
    kc.name AS ConstraintName,
    'UNIQUE' AS ConstraintType,
    'N/A' AS ConstraintDefinition
FROM sys.key_constraints kc
         INNER JOIN sys.tables t ON kc.parent_object_id = t.object_id
         INNER JOIN sys.index_columns ic ON kc.parent_object_id = ic.object_id AND kc.unique_index_id = ic.index_id
         INNER JOIN sys.columns col ON ic.object_id = col.object_id AND ic.column_id = col.column_id
WHERE kc.type = 'UQ'
  AND t.name IN ('PERSONAL_DETAILS', 'USERS', 'ADMIN', 'PASSENGER', 'DRIVER', 'LOCATIONN', 'STOPS', 'ROUTEE', 'BUS', 'TRIP', 'BOOKING', 'ALERTS', 'RESOLUTIONS', 'NOTIFICATIONS')

ORDER BY TableName, ConstraintType, ColumnName;
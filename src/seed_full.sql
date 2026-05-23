-- ================================================================
-- seed_full.sql  |  BOGO Smart Bus System — Full Data Seed (FIXED)
-- 30 Stops | 20 Buses | 20 Drivers | 20 Routes | 20 Trips
-- Constraints verified from DB:
--   Email    : LIKE '%_@gmail.com'          (must end with @gmail.com)
--   Password : LEN 8-20, contains digit + uppercase
--   CNIC     : exactly 13 digits (no dashes)
--   Registration (BUS) : VARCHAR(7) max
-- ================================================================
USE bogo;
GO

PRINT '=== BOGO Full Seed (Fixed) Started ===';
GO

-- ================================================================
-- SECTION 1 : LOCATIONS + STOPS  (Islamabad / Rawalpindi network)
-- ================================================================
IF (SELECT COUNT(*) FROM STOPS WHERE StopId >= 1001) = 0
BEGIN
    PRINT 'Seeding LOCATIONN for stops...';
    SET IDENTITY_INSERT LOCATIONN ON;
    INSERT INTO LOCATIONN (LocationId, Longitude, Latitude) VALUES
    (1001,73.0713,33.7322),(1002,73.0736,33.7236),(1003,73.0587,33.7215),
    (1004,73.0630,33.7179),(1005,73.0553,33.7151),(1006,73.0461,33.7095),
    (1007,73.0264,33.6998),(1008,73.0646,33.7129),(1009,73.0588,33.7071),
    (1010,73.0519,33.6997),(1011,73.0450,33.6969),(1012,73.0363,33.6925),
    (1013,73.0294,33.6878),(1014,73.0632,33.6832),(1015,73.0570,33.6881),
    (1016,73.0496,33.6824),(1017,73.0425,33.6769),(1018,73.0766,33.6972),
    (1019,73.0795,33.7162),(1020,73.0693,33.6578),(1021,73.0628,33.6348),
    (1022,73.0679,33.6465),(1023,73.0800,33.6300),(1024,73.0638,33.6211),
    (1025,73.0605,33.6152),(1026,73.0607,33.6097),(1027,73.0552,33.5995),
    (1028,73.0453,33.6215),(1029,73.1500,33.5200),(1030,73.1200,33.5600);
    SET IDENTITY_INSERT LOCATIONN OFF;

    PRINT 'Seeding STOPS...';
    SET IDENTITY_INSERT STOPS ON;
    INSERT INTO STOPS (StopId, StopName, LocationId, Connections) VALUES
    (1001,'Zero Point Interchange',1001,'1002,1003'),
    (1002,'Melody Market',         1002,'1001,1003,1019'),
    (1003,'Blue Area',             1003,'1001,1002,1004,1008'),
    (1004,'F-6 Markaz',            1004,'1003,1005,1008'),
    (1005,'F-7 Markaz',            1005,'1004,1006,1009'),
    (1006,'F-8 Markaz',            1006,'1005,1007,1010'),
    (1007,'F-10 Markaz',           1007,'1006,1012,1013'),
    (1008,'G-6 Markaz',            1008,'1003,1004,1009,1018'),
    (1009,'G-7 Markaz',            1009,'1005,1008,1010'),
    (1010,'G-8 Markaz',            1010,'1006,1009,1011,1014'),
    (1011,'G-9 Markaz',            1011,'1010,1012,1015'),
    (1012,'G-10 Markaz',           1012,'1007,1011,1013,1016'),
    (1013,'G-11 Markaz',           1013,'1007,1012,1017'),
    (1014,'H-8',                   1014,'1010,1015,1018'),
    (1015,'I-8 Markaz',            1015,'1011,1014,1016'),
    (1016,'I-9 Markaz',            1016,'1012,1015,1017'),
    (1017,'I-10 Markaz',           1017,'1013,1016,1020'),
    (1018,'Faizabad Interchange',  1018,'1008,1014,1019,1020'),
    (1019,'Aabpara Market',        1019,'1002,1008,1018'),
    (1020,'Pir Wadhai',            1020,'1017,1018,1021'),
    (1021,'Shamsabad',             1021,'1020,1022,1024'),
    (1022,'Chandni Chowk',         1022,'1021,1023'),
    (1023,'Murree Road Hub',       1023,'1018,1022'),
    (1024,'Kachari Chowk',         1024,'1021,1025'),
    (1025,'Committee Chowk',       1025,'1024,1026'),
    (1026,'Lal Kurti',             1026,'1025,1027'),
    (1027,'Rawalpindi Saddar',     1027,'1026,1028'),
    (1028,'Satellite Town',        1028,'1021,1027'),
    (1029,'Bahria Town',           1029,'1030'),
    (1030,'DHA Phase 1',           1030,'1027,1029');
    SET IDENTITY_INSERT STOPS OFF;

    PRINT 'STOPS: 30 rows seeded.';
END
ELSE PRINT 'STOPS: already seeded — skipped.';
GO

-- ================================================================
-- SECTION 2 : BUSES
-- Registration: VARCHAR(7) max  →  format 'XX-NNNN' (7 chars)
-- ================================================================
IF (SELECT COUNT(*) FROM BUS WHERE BusId >= 1001) = 0
BEGIN
    PRINT 'Seeding BUS...';
    SET IDENTITY_INSERT BUS ON;
    INSERT INTO BUS (BusId,BusCompany,Registration,RegistrationYear,BusStatus,Capacity,TyreHealth,EngineHealth,ChassisHealth) VALUES
    (1001,'BOGO Transit','BG-1001',2019,'ACTIVE',50,95.0,92.0,94.0),
    (1002,'BOGO Transit','BG-1002',2020,'ACTIVE',50,90.0,88.0,91.0),
    (1003,'BOGO Transit','BG-1003',2018,'ACTIVE',30,80.0,83.0,81.0),
    (1004,'BOGO Transit','BG-1004',2021,'ACTIVE',50,97.0,95.0,96.0),
    (1005,'BOGO Transit','BG-1005',2019,'ACTIVE',30,85.0,82.0,84.0),
    (1006,'Metro Exp',   'MT-1006',2020,'ACTIVE',50,91.0,89.0,90.0),
    (1007,'Metro Exp',   'MT-1007',2021,'ACTIVE',50,94.0,92.0,93.0),
    (1008,'Metro Exp',   'MT-1008',2019,'ACTIVE',30,77.0,80.0,78.0),
    (1009,'Metro Exp',   'MT-1009',2022,'ACTIVE',50,98.0,96.0,97.0),
    (1010,'Metro Exp',   'MT-1010',2020,'ACTIVE',30,86.0,83.0,85.0),
    (1011,'ISB Buses',   'IB-1011',2018,'ACTIVE',50,73.0,76.0,74.0),
    (1012,'ISB Buses',   'IB-1012',2019,'ACTIVE',50,82.0,79.0,81.0),
    (1013,'ISB Buses',   'IB-1013',2020,'ACTIVE',30,89.0,87.0,88.0),
    (1014,'ISB Buses',   'IB-1014',2021,'ACTIVE',50,92.0,91.0,93.0),
    (1015,'ISB Buses',   'IB-1015',2017,'ACTIVE',30,66.0,71.0,68.0),
    (1016,'RWP Trans',   'RW-1016',2019,'ACTIVE',50,84.0,82.0,83.0),
    (1017,'RWP Trans',   'RW-1017',2020,'ACTIVE',50,88.0,85.0,87.0),
    (1018,'RWP Trans',   'RW-1018',2021,'ACTIVE',30,93.0,91.0,92.0),
    (1019,'RWP Trans',   'RW-1019',2022,'ACTIVE',50,97.0,95.0,96.0),
    (1020,'RWP Trans',   'RW-1020',2018,'ACTIVE',30,71.0,74.0,72.0);
    SET IDENTITY_INSERT BUS OFF;

    PRINT 'BUS: 20 rows seeded.';
END
ELSE PRINT 'BUS: already seeded — skipped.';
GO

-- ================================================================
-- SECTION 3 : DRIVERS
-- Email    : must end with '@gmail.com'  (constraint: LIKE '%_@gmail.com')
-- Password : 8-20 chars, contains digit + uppercase  → 'Bogo1234'
-- CNIC     : exactly 13 digits (no dashes)
-- ================================================================
IF (SELECT COUNT(*) FROM PERSONAL_DETAILS WHERE PdId >= 1001) = 0
BEGIN
    PRINT 'Seeding PERSONAL_DETAILS...';
    SET IDENTITY_INSERT PERSONAL_DETAILS ON;
    INSERT INTO PERSONAL_DETAILS (PdId, Name, Email, Password, CNIC) VALUES
    (1001,'Ali Hassan',    'alihassan1@gmail.com',  'Bogo1234','3740101000001'),
    (1002,'Sara Ahmed',    'saraahmed2@gmail.com',  'Bogo1234','3740101000002'),
    (1003,'Usman Khan',    'usmankhan3@gmail.com',  'Bogo1234','3740101000003'),
    (1004,'Fatima Malik',  'fatimamalik4@gmail.com','Bogo1234','3740101000004'),
    (1005,'Bilal Qureshi', 'bilalq5@gmail.com',     'Bogo1234','3740101000005'),
    (1006,'Nadia Shah',    'nadiashah6@gmail.com',  'Bogo1234','3740101000006'),
    (1007,'Imran Butt',    'imranbutt7@gmail.com',  'Bogo1234','3740101000007'),
    (1008,'Ayesha Raza',   'ayesharaza8@gmail.com', 'Bogo1234','3740101000008'),
    (1009,'Zubair Awan',   'zubairawan9@gmail.com', 'Bogo1234','3740101000009'),
    (1010,'Hina Iqbal',    'hinaiqbal10@gmail.com', 'Bogo1234','3740101000010'),
    (1011,'Tariq Mehmood', 'tariqm11@gmail.com',    'Bogo1234','3740101000011'),
    (1012,'Sana Gillani',  'sanag12@gmail.com',     'Bogo1234','3740101000012'),
    (1013,'Asad Mirza',    'asadm13@gmail.com',     'Bogo1234','3740101000013'),
    (1014,'Robia Noor',    'robianoor14@gmail.com', 'Bogo1234','3740101000014'),
    (1015,'Fahad Siddiqui','fahads15@gmail.com',    'Bogo1234','3740101000015'),
    (1016,'Mehwish Rana',  'mehwishr16@gmail.com',  'Bogo1234','3740101000016'),
    (1017,'Kashif Lodhi',  'kashifl17@gmail.com',   'Bogo1234','3740101000017'),
    (1018,'Amna Bashir',   'amnab18@gmail.com',     'Bogo1234','3740101000018'),
    (1019,'Omer Farooq',   'omerf19@gmail.com',     'Bogo1234','3740101000019'),
    (1020,'Zunaira Taj',   'zunairat20@gmail.com',  'Bogo1234','3740101000020');
    SET IDENTITY_INSERT PERSONAL_DETAILS OFF;
    PRINT 'PERSONAL_DETAILS: 20 rows seeded.';

    PRINT 'Seeding USERS...';
    SET IDENTITY_INSERT USERS ON;
    INSERT INTO USERS (UserId, PdId) VALUES
    (1001,1001),(1002,1002),(1003,1003),(1004,1004),(1005,1005),
    (1006,1006),(1007,1007),(1008,1008),(1009,1009),(1010,1010),
    (1011,1011),(1012,1012),(1013,1013),(1014,1014),(1015,1015),
    (1016,1016),(1017,1017),(1018,1018),(1019,1019),(1020,1020);
    SET IDENTITY_INSERT USERS OFF;
    PRINT 'USERS: 20 rows seeded.';

    PRINT 'Seeding DRIVER...';
    INSERT INTO DRIVER (UserId, DriverID) VALUES
    (1001,'DRV-001'),(1002,'DRV-002'),(1003,'DRV-003'),(1004,'DRV-004'),
    (1005,'DRV-005'),(1006,'DRV-006'),(1007,'DRV-007'),(1008,'DRV-008'),
    (1009,'DRV-009'),(1010,'DRV-010'),(1011,'DRV-011'),(1012,'DRV-012'),
    (1013,'DRV-013'),(1014,'DRV-014'),(1015,'DRV-015'),(1016,'DRV-016'),
    (1017,'DRV-017'),(1018,'DRV-018'),(1019,'DRV-019'),(1020,'DRV-020');
    PRINT 'DRIVER: 20 rows seeded.';
END
ELSE PRINT 'PERSONAL_DETAILS/USERS/DRIVER: already seeded — skipped.';
GO

-- ================================================================
-- SECTION 4 : ROUTES  (ROUTEE table, Stop_IDs column)
-- ================================================================
IF (SELECT COUNT(*) FROM ROUTEE WHERE RouteId >= 1001) = 0
BEGIN
    PRINT 'Seeding ROUTEE...';
    SET IDENTITY_INSERT ROUTEE ON;
    INSERT INTO ROUTEE (RouteId, RouteName, Stop_IDs, EstimatedTimePerStop, Active) VALUES
    (1001,'Route 1  – Zero Point to Faizabad',     '1001,1002,1003,1008,1018',      120,1),
    (1002,'Route 2  – Blue Area to G-8',           '1003,1004,1005,1006,1010',      120,1),
    (1003,'Route 3  – F-8 to G-10 via G-9',        '1006,1010,1011,1012',           120,1),
    (1004,'Route 4  – Faizabad to Pir Wadhai',     '1018,1020,1021,1022',           150,1),
    (1005,'Route 5  – Chandni to Saddar',          '1022,1021,1024,1025,1026,1027', 150,1),
    (1006,'Route 6  – G-6 to I-9 via G-9/G-10',   '1008,1009,1010,1011,1012,1016', 120,1),
    (1007,'Route 7  – I-10 to Shamsabad',          '1017,1020,1021',                120,1),
    (1008,'Route 8  – H-8 to I-8 to I-9',         '1014,1015,1016',                120,1),
    (1009,'Route 9  – Saddar to Shamsabad',        '1027,1028,1021',                180,1),
    (1010,'Route 10 – Murree Road to Faizabad',    '1023,1022,1021,1020,1018',      150,1),
    (1011,'Route 11 – Faizabad to Zero Point',     '1018,1019,1002,1001',           120,1),
    (1012,'Route 12 – G-8 to Blue Area',           '1010,1009,1005,1004,1003',      120,1),
    (1013,'Route 13 – G-10 to F-8',               '1012,1011,1010,1006',           120,1),
    (1014,'Route 14 – I-9 to G-11',               '1016,1012,1013',                120,1),
    (1015,'Route 15 – Saddar to Chandni',          '1027,1026,1025,1024,1021,1022', 150,1),
    (1016,'Route 16 – G-11 to I-10',              '1013,1017',                     120,1),
    (1017,'Route 17 – Aabpara to G-6',            '1019,1018,1008',                120,1),
    (1018,'Route 18 – Satellite to Shamsabad',     '1028,1021',                     180,1),
    (1019,'Route 19 – F-6 to G-9',               '1004,1005,1006,1010,1011',       120,1),
    (1020,'Route 20 – G-9 to I-10',              '1011,1012,1013,1017',            120,1);
    SET IDENTITY_INSERT ROUTEE OFF;

    PRINT 'ROUTEE: 20 rows seeded.';
END
ELSE PRINT 'ROUTEE: already seeded — skipped.';
GO

-- ================================================================
-- SECTION 5 : ASSIGN buses + routes to drivers
-- ================================================================
IF EXISTS (SELECT 1 FROM DRIVER WHERE UserId = 1001 AND AssignedBusId IS NULL)
BEGIN
    PRINT 'Assigning buses and routes to drivers...';
    UPDATE DRIVER SET AssignedBusId=1001,AssignedRouteId=1001 WHERE UserId=1001;
    UPDATE DRIVER SET AssignedBusId=1002,AssignedRouteId=1002 WHERE UserId=1002;
    UPDATE DRIVER SET AssignedBusId=1003,AssignedRouteId=1003 WHERE UserId=1003;
    UPDATE DRIVER SET AssignedBusId=1004,AssignedRouteId=1004 WHERE UserId=1004;
    UPDATE DRIVER SET AssignedBusId=1005,AssignedRouteId=1005 WHERE UserId=1005;
    UPDATE DRIVER SET AssignedBusId=1006,AssignedRouteId=1006 WHERE UserId=1006;
    UPDATE DRIVER SET AssignedBusId=1007,AssignedRouteId=1007 WHERE UserId=1007;
    UPDATE DRIVER SET AssignedBusId=1008,AssignedRouteId=1008 WHERE UserId=1008;
    UPDATE DRIVER SET AssignedBusId=1009,AssignedRouteId=1009 WHERE UserId=1009;
    UPDATE DRIVER SET AssignedBusId=1010,AssignedRouteId=1010 WHERE UserId=1010;
    UPDATE DRIVER SET AssignedBusId=1011,AssignedRouteId=1011 WHERE UserId=1011;
    UPDATE DRIVER SET AssignedBusId=1012,AssignedRouteId=1012 WHERE UserId=1012;
    UPDATE DRIVER SET AssignedBusId=1013,AssignedRouteId=1013 WHERE UserId=1013;
    UPDATE DRIVER SET AssignedBusId=1014,AssignedRouteId=1014 WHERE UserId=1014;
    UPDATE DRIVER SET AssignedBusId=1015,AssignedRouteId=1015 WHERE UserId=1015;
    UPDATE DRIVER SET AssignedBusId=1016,AssignedRouteId=1016 WHERE UserId=1016;
    UPDATE DRIVER SET AssignedBusId=1017,AssignedRouteId=1017 WHERE UserId=1017;
    UPDATE DRIVER SET AssignedBusId=1018,AssignedRouteId=1018 WHERE UserId=1018;
    UPDATE DRIVER SET AssignedBusId=1019,AssignedRouteId=1019 WHERE UserId=1019;
    UPDATE DRIVER SET AssignedBusId=1020,AssignedRouteId=1020 WHERE UserId=1020;
    PRINT 'Driver assignments done.';
END
ELSE PRINT 'Driver assignments: already set — skipped.';
GO

-- ================================================================
-- SECTION 6 : TRIPS  (20 IN_PROGRESS trips)
-- RouteIds 1001-1020 → BusIds 1001-1020 → DriverIds 1001-1020
-- ================================================================
IF (SELECT COUNT(*) FROM TRIP WHERE RouteId >= 1001) = 0
BEGIN
    PRINT 'Seeding TRIP...';
    INSERT INTO TRIP (RouteId,BusId,DriverId,DepartureTime,ArrivalTime,CurrentStopIndex,SimulatedProgress,TripStatus) VALUES
    (1001,1001,1001,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1002,1002,1002,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1003,1003,1003,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1004,1004,1004,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1005,1005,1005,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1006,1006,1006,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1007,1007,1007,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1008,1008,1008,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1009,1009,1009,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1010,1010,1010,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1011,1011,1011,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1012,1012,1012,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1013,1013,1013,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1014,1014,1014,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1015,1015,1015,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1016,1016,1016,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1017,1017,1017,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1018,1018,1018,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1019,1019,1019,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS'),
    (1020,1020,1020,GETDATE(),DATEADD(HOUR,2,GETDATE()),0,0.0,'IN_PROGRESS');

    PRINT 'TRIP: 20 rows seeded.';
END
ELSE PRINT 'TRIP: already seeded — skipped.';
GO

-- ================================================================
-- SECTION 7 : SUMMARY
-- ================================================================
PRINT '=== Seed Complete — Final Counts ===';
SELECT 'STOPS'             AS [Table], COUNT(*) AS [Rows Here] FROM STOPS             WHERE StopId  >= 1001 UNION ALL
SELECT 'BUS',                           COUNT(*)               FROM BUS               WHERE BusId   >= 1001 UNION ALL
SELECT 'PERSONAL_DETAILS',              COUNT(*)               FROM PERSONAL_DETAILS  WHERE PdId    >= 1001 UNION ALL
SELECT 'USERS',                         COUNT(*)               FROM USERS             WHERE UserId  >= 1001 UNION ALL
SELECT 'DRIVER',                        COUNT(*)               FROM DRIVER            WHERE UserId  >= 1001 UNION ALL
SELECT 'ROUTEE',                        COUNT(*)               FROM ROUTEE            WHERE RouteId >= 1001 UNION ALL
SELECT 'TRIP (IN_PROGRESS)',            COUNT(*)               FROM TRIP              WHERE RouteId >= 1001 AND TripStatus = 'IN_PROGRESS';
GO

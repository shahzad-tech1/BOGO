USE BOGO;
GO

-- Users Table
CREATE TABLE Users (
    userID INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phoneNumber VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    userType VARCHAR(50) NOT NULL -- 'Admin', 'Driver', 'Passenger'
);

-- Drivers Table
CREATE TABLE Drivers (
    userID INT PRIMARY KEY FOREIGN KEY REFERENCES Users(userID),
    licenseNumber VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) DEFAULT 'INACTIVE'
);

-- Passengers Table
CREATE TABLE Passengers (
    userID INT PRIMARY KEY FOREIGN KEY REFERENCES Users(userID)
);

-- Admins Table
CREATE TABLE Admins (
    userID INT PRIMARY KEY FOREIGN KEY REFERENCES Users(userID)
);

-- Routes Table
CREATE TABLE Routes (
    routeID INT PRIMARY KEY
);

-- Stops Table
CREATE TABLE Stops (
    stopID INT PRIMARY KEY,
    stopName VARCHAR(255) NOT NULL,
    active BIT DEFAULT 1,
    locationX INT,
    locationY INT
);

-- RouteStops (Connections)
CREATE TABLE RouteStops (
    routeID INT FOREIGN KEY REFERENCES Routes(routeID),
    stopID INT FOREIGN KEY REFERENCES Stops(stopID),
    price DECIMAL(10,2),
    sequenceNumber INT,
    PRIMARY KEY (routeID, stopID)
);

-- Buses Table
CREATE TABLE Buses (
    busID INT PRIMARY KEY,
    capacity INT NOT NULL,
    currentCapacity INT NOT NULL,
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    locationX INT,
    locationY INT,
    routeID INT NULL FOREIGN KEY REFERENCES Routes(routeID),
    driverID INT NULL FOREIGN KEY REFERENCES Drivers(userID)
);

-- Bookings Table
CREATE TABLE Bookings (
    bookingID INT PRIMARY KEY IDENTITY(1,1),
    passengerID INT FOREIGN KEY REFERENCES Passengers(userID),
    busID INT FOREIGN KEY REFERENCES Buses(busID),
    cost DECIMAL(10,2),
    status VARCHAR(50) DEFAULT 'PENDING',
    paymentMethod VARCHAR(50),
    qrCode VARCHAR(255),
    createdAt DATETIME DEFAULT GETDATE(),
    active BIT DEFAULT 1
);

-- Notifications Table
CREATE TABLE Notifications (
    notificationID INT PRIMARY KEY IDENTITY(1,1),
    userID INT FOREIGN KEY REFERENCES Users(userID),
    message NVARCHAR(MAX) NOT NULL,
    isRead BIT DEFAULT 0,
    createdAt DATETIME DEFAULT GETDATE()
);

GO

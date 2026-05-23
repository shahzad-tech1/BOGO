# Smart Public Bus Transport System

A demand-based smart public transportation management system developed using Java, JavaFX, JDBC, and Microsoft SQL Server. The project introduces a lift-style routing model where buses only stop at stations with confirmed passenger bookings, improving operational efficiency and reducing unnecessary travel delays.


## Overview

Traditional public transport systems operate on fixed-stop models, causing unnecessary stops, fuel waste, overcrowding, and inefficient route management. This project provides a smarter alternative through dynamic routing, real-time capacity monitoring, digital payments, and role-based system management.

The system supports three primary user roles:

- Passenger
- Driver
- Administrator

Each role has a dedicated interface connected to a centralized backend database.



## Key Features

### Passenger Features
- User registration and login
- Ride booking and cancellation
- QR code-based ride confirmation
- Digital e-wallet management
- Fare deduction and wallet top-up
- Multi-route pathfinding
- Real-time route and map visualization
- Live bus tracking
- Feedback submission for drivers and buses

### Driver Features
- Real-time stop instructions
- Route and schedule management
- Passenger payment verification
- Bus capacity status updates
- Stop completion tracking
- Emergency and issue reporting

### Administrator Features
- Bus and route management
- Driver assignment and monitoring
- Stop and route modification
- Live operational monitoring
- Booking and transaction management
- Driver issue handling
- System-wide record maintenance

### Smart Routing Features
- Demand-based lift-style routing
- Dynamic stop selection
- Early return logic for buses
- Opposite-direction boarding restriction
- Real-time capacity monitoring
- Overcrowding prevention

---

## Technology Stack
 
Java: Core application development
JavaFX: Desktop graphical user interface 
JDBC: Database connectivity 
Microsoft SQL Server: Database management 
IntelliJ IDEA: Development environment 


## Objectives

- Reduce unnecessary bus stops and fuel consumption
- Improve passenger travel predictability
- Provide tourist-friendly route guidance
- Prevent bus overcrowding
- Enable centralized transport management
- Deliver a modern smart transportation solution



## Installation

### Prerequisites

- Java JDK 17+ (or your version)
- IntelliJ IDEA
- Microsoft SQL Server
- JDBC Driver for SQL Server

### Steps

1. Clone the repository

```bash
git clone https://github.com/shahzad-tech1/Smart-Public-Bus-Transport-System.git
```

2. Open the project in IntelliJ IDEA

3. Configure database connection settings

4. Import the SQL database schema

5. Run the JavaFX application



## Database

The system uses Microsoft SQL Server for:

- User management
- Booking records
- Route management
- Wallet transactions
- Capacity tracking
- Operational logs



## Academic Information

Developed as part of:

SE2002 — Software Design and Architecture 
FAST – National University of Computer and Emerging Sciences



## Author

--Shahzad Ahmad

## License

This project is developed for academic purposes.
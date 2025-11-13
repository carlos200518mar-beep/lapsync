-- Create the database
CREATE DATABASE LapsyncDB;
GO

USE LapsyncDB;
GO

-- Users table
CREATE TABLE users (
    id INT PRIMARY KEY IDENTITY(1,1),
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    student_id NVARCHAR(20),         -- Only for students
    career NVARCHAR(100),
    national_id NVARCHAR(20),
    password_hash NVARCHAR(255),     -- Only for admins/superadmins
    role NVARCHAR(20) NOT NULL CHECK (role IN ('student', 'admin', 'superadmin')),
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);
GO

-- Laptops table
CREATE TABLE laptops (
    id INT PRIMARY KEY IDENTITY(1,1),
    asset_tag NVARCHAR(50) NOT NULL UNIQUE,
    brand NVARCHAR(50),
    model NVARCHAR(50),
    status NVARCHAR(30) CHECK (status IN ('available', 'loaned', 'reserved', 'repair', 'retired')) NOT NULL,
    condition_description NVARCHAR(255),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);
GO

-- Loans table
CREATE TABLE loans (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    laptop_id INT NOT NULL FOREIGN KEY REFERENCES laptops(id),
    requested_at DATETIME DEFAULT GETDATE(),
    approved_at DATETIME,
    delivered_at DATETIME,
    returned_at DATETIME,
    requested_hours INT,
    status NVARCHAR(30) NOT NULL CHECK (status IN ('pending', 'approved', 'rejected', 'active', 'completed')),
    terms_accepted BIT DEFAULT 0
);
GO

-- Penalties table
CREATE TABLE penalties (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    type NVARCHAR(100) NOT NULL,
    description NVARCHAR(255),
    created_at DATETIME DEFAULT GETDATE(),
    resolved_at DATETIME,
    is_resolved BIT DEFAULT 0,
    fine_amount DECIMAL(10,2) DEFAULT 0.00
);
GO
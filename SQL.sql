CREATE DATABASE J6Assignment;
GO
USE J6Assignment;
GO

-- 1. Danh mục
CREATE TABLE Categories (
    Id char(4) PRIMARY KEY,
    Name nvarchar(50) NOT NULL
);

-- 2. Sản phẩm
CREATE TABLE Products (
    Id int IDENTITY(1,1) PRIMARY KEY,
    Name nvarchar(50) NOT NULL,
    Image nvarchar(50),
    Price float NOT NULL,
    CreateDate date DEFAULT getdate(),
    Available bit DEFAULT 1,
    CategoryId char(4) FOREIGN KEY REFERENCES Categories(Id)
);

-- 3. Tài khoản
CREATE TABLE Accounts (
    Username nvarchar(50) PRIMARY KEY,
    Password nvarchar(50) NOT NULL,
    Fullname nvarchar(50) NOT NULL,
    Email nvarchar(50) NOT NULL,
    Photo nvarchar(50)
);

-- 4. Vai trò
CREATE TABLE Roles (
    Id nvarchar(10) PRIMARY KEY,
    Name nvarchar(50) NOT NULL
);

-- 5. Phân quyền (N-N giữa Account & Role)
CREATE TABLE Authorities (
    Id int IDENTITY(1,1) PRIMARY KEY,
    Username nvarchar(50) FOREIGN KEY REFERENCES Accounts(Username),
    RoleId nvarchar(10) FOREIGN KEY REFERENCES Roles(Id)
);

-- 6. Đơn hàng
CREATE TABLE Orders (
    Id bigint IDENTITY(1,1) PRIMARY KEY,
    Username nvarchar(50) FOREIGN KEY REFERENCES Accounts(Username),
    CreateDate datetime DEFAULT getdate(),
    Address nvarchar(100) NOT NULL
);

-- 7. Chi tiết đơn hàng
CREATE TABLE OrderDetails (
    Id bigint IDENTITY(1,1) PRIMARY KEY,
    OrderId bigint FOREIGN KEY REFERENCES Orders(Id),
    ProductId int FOREIGN KEY REFERENCES Products(Id),
    Price float NOT NULL,
    Quantity int NOT NULL
);
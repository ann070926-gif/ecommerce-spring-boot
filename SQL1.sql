USE J6Assignment;
GO
-- 1. Thêm 4 Danh m?c (Category) - Thêm 2 lo?i m?i
INSERT INTO Categories (Id, Name) VALUES 
('1000', N'??ng h? thông minh'),
('1001', N'Máy tính xách tay'),
('1002', N'?i?n tho?i di ??ng'),
('1003', N'Tai nghe không dây');
GO

-- 2. Thêm 10 S?n ph?m m?u (Product) - B? ?nh
INSERT INTO Products (Name, Image, Price, CreateDate, Available, CategoryId) VALUES 
(N'Apple Watch Series 8', 'static/images/apple-watch.jpg', 500, GETDATE(), 1, '1000'),
(N'Samsung Galaxy Watch 5', 'static/images/galaxy-watch.jpg', 350, GETDATE(), 1, '1000'),
(N'Fitbit Sense 2', 'static/images/fitbit-sense.jpg', 300, GETDATE(), 1, '1000'),
(N'Garmin Epix Gen 2', 'static/images/garmin-epix.jpg', 650, GETDATE(), 1, '1000'),
(N'Dell XPS 15', 'static/images/dell-xps.jpg', 1500, GETDATE(), 1, '1001'),
(N'MacBook Pro M2', 'static/images/macbook-pro.jpg', 2000, GETDATE(), 1, '1001'),
(N'ASUS VivoBook 15', 'static/images/asus-vivobook.jpg', 800, GETDATE(), 1, '1001'),
(N'IPhone 15 Pro Max', 'static/images/iphone15.jpg', 1200, GETDATE(), 1, '1002'),
(N'Samsung Galaxy S24', 'static/images/galaxy-s24.jpg', 999, GETDATE(), 1, '1002'),
(N'Sony WH-1000XM5', 'static/images/sony-headphones.jpg', 399, GETDATE(), 1, '1003');
GO

-- 3. Thêm Tài kho?n m?u (Account) - ?? ??ng nh?p
INSERT INTO Accounts (Username, Password, Fullname, Email, Photo) VALUES 
('admin', '123', N'Giám ??c', 'admin@fpt.edu.vn', NULL),
('teonv', '123', N'Nguy?n V?n Tèo', 'teonv@fpt.edu.vn', NULL);
GO

-- 4. Thêm Vai trò (Role) - GI? NGUYÊN
INSERT INTO Roles (Id, Name) VALUES 
('ADMIN', N'Qu?n tr? viên'),
('USER', N'Khách hàng');
GO

-- 5. C?p quy?n cho tài kho?n (Authority) - GI? NGUYÊN
INSERT INTO Authorities (Username, RoleId) VALUES 
('admin', 'ADMIN'),
('teonv', 'USER');
GO

-- 6. Thêm 3 ??n hàng m?u (Order)
INSERT INTO Orders (Username, CreateDate, Address) VALUES 
('teonv', GETDATE(), N'123 ???ng Láng, Hà N?i'),
('teonv', GETDATE(), N'456 Nguy?n Hu?, TP H? Chí Minh'),
('admin', GETDATE(), N'789 Tr?n H?ng ??o, ?à N?ng');
GO

-- 7. Thêm chi ti?t ??n hàng (OrderDetails)
INSERT INTO OrderDetails (OrderId, ProductId, Price, Quantity) VALUES 
(1, 1, 500, 1),
(1, 9, 399, 1),
(2, 5, 1500, 1),
(2, 3, 300, 2),
(3, 8, 1200, 1);
GO

-- Hi?n th? d? li?u ?ã thêm
SELECT * FROM Categories;
SELECT * FROM Products;
SELECT * FROM Accounts;
SELECT * FROM Roles;
SELECT * FROM Authorities;
SELECT * FROM Orders;
SELECT * FROM OrderDetails;
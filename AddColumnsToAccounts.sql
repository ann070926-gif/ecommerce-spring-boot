USE J6Assignment;
GO

-- Thêm cột Address và Phone vào bảng Accounts
ALTER TABLE Accounts ADD 
    [Address] nvarchar(255),
    Phone nvarchar(20);

GO

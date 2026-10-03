USE smart_pantry;
GO

IF OBJECT_ID('dbo.usuarios', 'U') IS NULL
BEGIN
    CREATE TABLE usuarios (
        id_usuario INT IDENTITY(1,1) PRIMARY KEY,
        nombre VARCHAR(100) NOT NULL,
        correo VARCHAR(150) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        rol VARCHAR(30) NOT NULL DEFAULT 'Usuario',
        fecha_creacion DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;
GO

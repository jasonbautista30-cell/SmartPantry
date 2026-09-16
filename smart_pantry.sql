-- ============================================================
-- SMART PANTRY - Base de Datos
-- Proyecto: Smart Pantry
-- Institución: ESFE-AGAPE
-- Equipo: NEXO DEVS
-- Motor: Microsoft SQL Server
-- Servidor: localhost\SQLEXPRESS
-- ============================================================

-- Usar la base de datos existente
USE smart_pantry;
GO

-- ============================================================
-- TABLA 1: categorias
-- Almacena las categorías de los productos.
-- ============================================================
CREATE TABLE categorias (
    id_categoria  INT           IDENTITY(1,1) PRIMARY KEY,
    nombre        VARCHAR(50)   NOT NULL UNIQUE,
    descripcion   VARCHAR(200)  NULL
);
GO

-- ============================================================
-- TABLA 2: productos
-- Catálogo general de productos.
-- ============================================================
CREATE TABLE productos (
    id_producto    INT            IDENTITY(1,1) PRIMARY KEY,
    id_categoria   INT            NULL,
    nombre         VARCHAR(100)   NOT NULL,
    codigo_barras  VARCHAR(50)    UNIQUE,
    unidad_medida  VARCHAR(20)    DEFAULT 'unidades',

    CONSTRAINT fk_productos_categoria
        FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);
GO

-- ============================================================
-- TABLA 3: inventario
-- Controla los productos disponibles en la despensa.
-- Permite implementar:
--   * Alertas de bajo stock (cantidad < cantidad_minima)
--   * Alertas de productos próximos a vencer (fecha_vencimiento)
--   * Ubicación del producto dentro de la despensa (ubicacion)
-- ============================================================
CREATE TABLE inventario (
    id_inventario     INT          IDENTITY(1,1) PRIMARY KEY,
    id_producto       INT          NOT NULL,
    cantidad          INT          NOT NULL DEFAULT 0,
    cantidad_minima   INT          NOT NULL DEFAULT 2,
    fecha_vencimiento DATE         NULL,
    ubicacion         VARCHAR(50)  DEFAULT 'Alacena',
    fecha_ingreso     DATETIME     DEFAULT GETDATE(),

    CONSTRAINT fk_inventario_producto
        FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
        ON DELETE CASCADE,

    CONSTRAINT chk_cantidad_no_negativa
        CHECK (cantidad >= 0),

    CONSTRAINT chk_cantidad_minima_no_negativa
        CHECK (cantidad_minima >= 0)
);
GO

-- ============================================================
-- TABLA 4: listas_compras
-- Encabezados de las listas de compras.
-- ============================================================
CREATE TABLE listas_compras (
    id_lista       INT            IDENTITY(1,1) PRIMARY KEY,
    nombre_lista   VARCHAR(100)   DEFAULT 'Mi Lista de Compras',
    estado         VARCHAR(20)    DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME       DEFAULT GETDATE(),

    CONSTRAINT chk_estado_valido
        CHECK (estado IN ('PENDIENTE', 'COMPLETADA'))
);
GO

-- ============================================================
-- TABLA 5: detalle_lista_compras
-- Productos incluidos en cada lista de compras.
-- ============================================================
CREATE TABLE detalle_lista_compras (
    id_detalle         INT      IDENTITY(1,1) PRIMARY KEY,
    id_lista           INT      NOT NULL,
    id_producto        INT      NOT NULL,
    cantidad_a_comprar INT      NOT NULL DEFAULT 1,
    comprado           BIT      DEFAULT 0,

    CONSTRAINT fk_detalle_lista
        FOREIGN KEY (id_lista) REFERENCES listas_compras(id_lista)
        ON DELETE CASCADE,

    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
        ON DELETE CASCADE,

    CONSTRAINT chk_cantidad_a_comprar_positiva
        CHECK (cantidad_a_comprar > 0)
);
GO

-- ============================================================
-- DATOS INICIALES DE PRUEBA
-- ============================================================

-- -------------------------------------------------------
-- Categorías
-- -------------------------------------------------------
INSERT INTO categorias (nombre, descripcion) VALUES
    ('Lácteos',   'Productos derivados de la leche'),
    ('Bebidas',   'Bebidas embotelladas, jugos y refrescos'),
    ('Granos',    'Arroz, frijoles, lentejas y cereales'),
    ('Carnes',    'Carnes rojas, pollo, cerdo y embutidos'),
    ('Frutas',    'Frutas frescas y deshidratadas'),
    ('Verduras',  'Verduras y hortalizas frescas'),
    ('Limpieza',  'Productos de limpieza para el hogar');
GO

-- -------------------------------------------------------
-- Productos
-- -------------------------------------------------------
SET IDENTITY_INSERT productos ON;

INSERT INTO productos (id_producto, id_categoria, nombre, codigo_barras, unidad_medida) VALUES
    (1,  1, 'Leche entera 1L',            '7501001001001', 'litros'),
    (2,  1, 'Queso fresco',               '7501001001002', 'gramos'),
    (3,  1, 'Yogur natural',              '7501001001003', 'unidades'),
    (4,  2, 'Jugo de naranja 1L',         '7501002001001', 'litros'),
    (5,  2, 'Agua purificada 1L',         '7501002001002', 'litros'),
    (6,  3, 'Arroz blanco 1kg',           '7501003001001', 'kilogramos'),
    (7,  3, 'Frijoles rojos 1kg',         '7501003001002', 'kilogramos'),
    (8,  3, 'Lentejas 500g',              '7501003001003', 'gramos'),
    (9,  4, 'Pechuga de pollo',           '7501004001001', 'kilogramos'),
    (10, 4, 'Carne molida de res',        '7501004001002', 'kilogramos'),
    (11, 5, 'Bananos',                    '7501005001001', 'unidades'),
    (12, 5, 'Manzanas rojas',             '7501005001002', 'unidades'),
    (13, 6, 'Tomates',                    '7501006001001', 'unidades'),
    (14, 6, 'Cebolla blanca',             '7501006001002', 'unidades'),
    (15, 6, 'Papa',                       '7501006001003', 'kilogramos'),
    (16, 7, 'Jabón líquido para trastes', '7501007001001', 'unidades'),
    (17, 7, 'Desinfectante multiusos',    '7501007001002', 'litros');

SET IDENTITY_INSERT productos OFF;
GO

-- -------------------------------------------------------
-- Inventario
-- -------------------------------------------------------
INSERT INTO inventario (id_producto, cantidad, cantidad_minima, fecha_vencimiento, ubicacion) VALUES
    (1,  3,  2, '2026-10-15', 'Refrigerador'),
    (2,  1,  1, '2026-09-28', 'Refrigerador'),
    (3,  4,  2, '2026-10-05', 'Refrigerador'),
    (4,  2,  2, '2026-10-20', 'Refrigerador'),
    (5,  6,  3, '2027-01-01', 'Alacena'),
    (6,  2,  1, NULL,         'Alacena'),
    (7,  1,  2, NULL,         'Alacena'),
    (8,  3,  2, NULL,         'Alacena'),
    (9,  1,  1, '2026-09-20', 'Congelador'),
    (10, 1,  1, '2026-09-22', 'Congelador'),
    (11, 6,  3, NULL,         'Frutero'),
    (12, 4,  3, NULL,         'Frutero'),
    (13, 5,  3, NULL,         'Refrigerador'),
    (14, 3,  2, NULL,         'Alacena'),
    (15, 4,  2, NULL,         'Alacena'),
    (16, 1,  1, NULL,         'Alacena'),
    (17, 2,  1, NULL,         'Alacena');
GO

-- -------------------------------------------------------
-- Listas de compras
-- -------------------------------------------------------
INSERT INTO listas_compras (nombre_lista, estado) VALUES
    ('Compras de la semana',  'PENDIENTE'),
    ('Lista del mes pasado',  'COMPLETADA');
GO

-- -------------------------------------------------------
-- Detalle de listas de compras
-- -------------------------------------------------------
INSERT INTO detalle_lista_compras (id_lista, id_producto, cantidad_a_comprar, comprado) VALUES
    (1, 1,  2, 0),
    (1, 6,  1, 0),
    (1, 9,  2, 1),
    (1, 13, 4, 0),
    (2, 5,  3, 1),
    (2, 7,  2, 1),
    (2, 16, 1, 1);
GO

-- ============================================================
-- FIN DEL SCRIPT
-- Smart Pantry © NEXO DEVS - ESFE-AGAPE
-- ============================================================

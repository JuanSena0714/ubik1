create database products_db;
use products_db;
CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(160) NOT NULL,
  price DECIMAL(15,2) NOT NULL,
  stock DECIMAL(8,2) NOT NULL
);
insert into products(name,price,stock) values
("papas",2500,15),
("Doritos",3100,10);
select * from products;

create database venta_db;
use venta_db;
create table if not exists venta(
id bigint auto_increment primary key,
codigo varchar(160) not null,
cliente varchar(160) not null,
total decimal(10,2) NOT null);
insert into venta(codigo,cliente,total) values
("VE001","JULIAN GORDILLO", 25000),
("VE002","JUANA",12500);
DROP TABLE venta;
select * from venta;

create database detalleVenta_db;
use detalleVenta_db;
create table if not exists detalleVenta(
id bigint auto_increment primary key,
cod varchar(160) not null,
id_venta bigint not null,
id_producto bigint not null,
cantidad decimal(10,2),
precio_unitario decimal(10,2),
subtotal decimal(10,2)
);
insert into detalleVenta(cod,id_venta,id_producto,cantidad,precio_unitario,subtotal)values
('DV-001', 1, 101, 2.00, 15000.00, 30000.00),
('DV-002', 1, 102, 1.00, 25000.00, 25000.00),
('DV-003', 2, 103, 3.00, 12000.00, 36000.00);
select * from detalleVenta;


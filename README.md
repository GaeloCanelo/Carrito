# 🛒 Sistema de Carrito de Compras (Cliente-Servidor)

Este proyecto implementa un sistema de ventas distribuido basado en la arquitectura **Cliente-Servidor** utilizando **Java Sockets**. El sistema permite la gestión de inventario en tiempo real, transmisión de archivos (imágenes) y generación de comprobantes de compra en PDF.

**Asignatura:** Aplicaciones para Comunicaciones en Red  
**Tecnologías:** Java (Sockets, Serialization, IO), iText PDF Library.

---

## 🚀 Características Principales

### 🖥️ Servidor
* **Persistencia de Datos:** Carga y guarda el inventario mediante serialización de objetos (`productos.dat`).
* **Gestión de Conexiones:** Atiende clientes de forma secuencial.
* **Transmisión de Archivos:** Envía imágenes de productos bajo demanda al cliente.
* **Control de Stock:** Valida existencias y actualiza el inventario global en tiempo real tras cada compra.
* **Logs Centralizados:** Monitorea la actividad (conexiones, compras, productos agotados).

### 👤 Cliente
* **Interfaz de Consola:** Menú interactivo con limpieza de pantalla para una mejor UX.
* **Carrito de Compras:** Permite agregar, visualizar y modificar cantidades de productos antes de pagar.
* **Sincronización:** Descarga automática de imágenes (`Lazy/Pre-load`) y validación de stock antes de solicitar recursos.
* **Generación de Recibos:** Crea un **ticket de compra en PDF** (con tablas y diseño estético) utilizando la librería **iText**.
* **Configuración Flexible:** Permite ingresar IP y Puerto manualmente al iniciar.
* **Modo Kiosco:** Limpieza automática de archivos temporales al cerrar sesión.

---

## 📂 Estructura del Proyecto

El proyecto debe mantener la siguiente estructura de directorios para su correcto funcionamiento:

```text
ProyectoCarrito/
├── lib/
│   └── itextpdf-5.5.13.2.jar   <-- Librería externa para PDFs
├── src/
│   ├── Servidor.java           <-- Lógica del Servidor
│   ├── Cliente.java            <-- Lógica del Cliente
│   ├── Producto.java           <-- Clase Serializable (Modelo)
│   ├── GeneradorCatalogo.java  <-- Utilidad para crear inventario inicial
│   ├── productos.dat           <-- Base de datos (generada automáticamente)
│   ├── Origen_SV/              <-- Carpeta de Imágenes del SERVIDOR
│   │   ├── churrumais.jpg
│   │   └── ...
│   └── Destino_CLT/            <-- Carpeta temporal del CLIENTE (se crea sola)
└── README.md
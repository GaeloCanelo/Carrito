import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

// Librerías básicas para PDF
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
// Librerías para el diseño bonito (Tablas, Fuentes, Colores)
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Phrase;

// Librerías para Fecha
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Cliente {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // --- LIMPIEZA INICIAL ---
            File reciboViejo = new File("Destino_CLT/Recibo_Compra.pdf");
            if (reciboViejo.exists()) {
                reciboViejo.delete();
            }
            
            // --- CONFIGURACIÓN DE CONEXIÓN ---
            limpiarPantalla(); 
            System.out.println("=== CONFIGURACIÓN DE CONEXIÓN ===");
            System.out.print("Ingresa la IP del Servidor (Enter para 'localhost'): ");
            String ip = scanner.nextLine();
            if (ip.isEmpty()) {
                ip = "localhost";
            }

            System.out.print("Ingresa el Puerto (Enter para '6040'): ");
            String puertoStr = scanner.nextLine();
            int puerto = 6040;
            try {
                if (!puertoStr.isEmpty()) {
                    puerto = Integer.parseInt(puertoStr);
                }
            } catch (NumberFormatException e) {
                System.out.println("Puerto no válido, usando 6040 por defecto.");
            }

            System.out.println("\nIntentando conectar a " + ip + ":" + puerto + "...");
            Socket socket = new Socket(ip, puerto);
            System.out.println("¡Conectado al servidor!");

            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            System.out.println("Descargando catálogo...");
            ArrayList<Producto> catalogo = (ArrayList<Producto>) entrada.readObject();
            
            System.out.println("Descargando imágenes del servidor...");
            new File("Destino_CLT").mkdirs();

            for (Producto p : catalogo) {
                salida.writeObject("PEDIR_IMAGEN");
                salida.writeObject(p.getNombreImagen());
                
                long tamanoArchivo = (long) entrada.readObject();
                
                if (tamanoArchivo > 0) {
                    FileOutputStream fos = new FileOutputStream("Destino_CLT/" + p.getNombreImagen());
                    byte[] buffer = new byte[4096];
                    int leido;
                    long totalLeido = 0;
                    while (totalLeido < tamanoArchivo) {
                         leido = entrada.read(buffer);
                         fos.write(buffer, 0, leido);
                         totalLeido += leido;
                    }
                    fos.close(); 
                }
            }
            System.out.println("¡Imágenes actualizadas en 'Destino_CLT'!");
            
            try { Thread.sleep(1500); } catch (InterruptedException e) {}

            ArrayList<Producto> carrito = new ArrayList<>();
            boolean continuar = true;

            // --- MENÚ PRINCIPAL ---
            while (continuar) {
                limpiarPantalla(); // Limpia antes de mostrar el menú
                
                System.out.println("\n--- TIENDITA DE LA ESQUINA ---");
                System.out.println("1. Ver Catálogo");
                System.out.println("2. Agregar producto al carrito");
                System.out.println("3. Ver mi Carrito");
                System.out.println("4. Pagar y Salir");
                System.out.println("5. Modificar cantidad en carrito");
                System.out.println("0. Salir sin comprar");
                System.out.print("Elige una opción: ");
                
                if (scanner.hasNextInt()) {
                    int opcion = scanner.nextInt();
                    scanner.nextLine(); // Limpiar buffer

                    switch (opcion) {
                        case 1: 
                            limpiarPantalla(); // <--- LIMPIEZA EXTRA
                            System.out.println("\n--- CATÁLOGO DISPONIBLE ---");
                            for (Producto p : catalogo) {
                                if (p.getStock() > 0) {
                                    System.out.println(p); 
                                }
                            }
                            System.out.println("\nPresiona Enter para volver al menú...");
                            scanner.nextLine();
                            break;

                        case 2: 
                            limpiarPantalla(); // <--- LIMPIEZA EXTRA
                            System.out.println("\n--- AGREGAR PRODUCTO ---");
                            // Mostramos catálogo rápido para referencia
                            for (Producto p : catalogo) {
                                if (p.getStock() > 0) System.out.println(p);
                            }
                            System.out.println("------------------------");
                            
                            System.out.print("Ingresa el ID del producto: ");
                            int idBuscado = scanner.nextInt();
                            scanner.nextLine();
                            
                            Producto productoSeleccionado = null;
                            for (Producto p : catalogo) {
                                if (p.getId() == idBuscado) {
                                    productoSeleccionado = p;
                                    break;
                                }
                            }
                            
                            if (productoSeleccionado != null) {
                                if (productoSeleccionado.getStock() > 0) {
                                    System.out.println("Seleccionaste: " + productoSeleccionado.getNombre());
                                    System.out.println("Disponibles: " + productoSeleccionado.getStock());
                                    System.out.print("¿Cuántos quieres llevar?: ");
                                    int cantidad = scanner.nextInt();
                                    scanner.nextLine();
                                    
                                    if (cantidad > 0 && cantidad <= productoSeleccionado.getStock()) {
                                        Producto itemCarrito = new Producto(
                                            productoSeleccionado.getId(),
                                            productoSeleccionado.getNombre(),
                                            productoSeleccionado.getDescripcion(),
                                            productoSeleccionado.getPrecio(),
                                            cantidad, 
                                            productoSeleccionado.getNombreImagen()
                                        );
                                        carrito.add(itemCarrito);
                                        salida.writeObject("AGREGAR");
                                        salida.writeObject(productoSeleccionado.getId());
                                        salida.writeObject(cantidad);
                                        System.out.println("¡Agregado al carrito exitosamente!");
                                    } else {
                                        System.out.println("Error: Cantidad inválida.");
                                    }
                                } else {
                                    System.out.println("Error: Producto AGOTADO.");
                                }
                            } else {
                                System.out.println("Error: Producto no encontrado.");
                            }
                            System.out.println("\nPresiona Enter para continuar...");
                            scanner.nextLine();
                            break;

                        case 3: 
                            limpiarPantalla(); // <--- LIMPIEZA EXTRA
                            System.out.println("\n--- TU CARRITO DE COMPRAS ---");
                            if (carrito.isEmpty()) {
                                System.out.println("Tu carrito está vacío.");
                            } else {
                                double granTotal = 0;
                                for (Producto p : carrito) {
                                    int cant = p.getStock();
                                    double subtotal = p.getPrecio() * cant;
                                    granTotal += subtotal;
                                    System.out.println(cant + "x " + p.getNombre() + " ($" + p.getPrecio() + ") = $" + subtotal);
                                }
                                System.out.println("--------------------------------");
                                System.out.println("TOTAL A PAGAR: $" + granTotal);
                            }
                            System.out.println("\nPresiona Enter para volver al menú...");
                            scanner.nextLine();
                            break;

                        case 4: // PAGAR
                            limpiarPantalla(); // <--- LIMPIEZA EXTRA
                            if (!carrito.isEmpty()) {
                                double totalPagar = 0;
                                for (Producto p : carrito) {
                                    totalPagar += (p.getPrecio() * p.getStock());
                                }

                                salida.writeObject("PAGAR");
                                
                                catalogo = (ArrayList<Producto>) entrada.readObject();
                                System.out.println(" (Inventario actualizado recibido) ");
                                
                                for (Producto p : catalogo) {
                                    if (p.getStock() == 0) {
                                        File foto = new File("Destino_CLT/" + p.getNombreImagen());
                                        if (foto.exists()) {
                                            foto.delete(); 
                                        }
                                    }
                                }

                                System.out.println("\nProcesando compra...");
                                generarReciboPDF(carrito, totalPagar);
                                salida.writeObject("LOG_RECIBO");
                                
                                System.out.println("¡Pago realizado con éxito!");
                                System.out.println("Gracias por su compra.");
                                
                                carrito.clear();
                            } else {
                                System.out.println("No puedes pagar: Tu carrito está vacío.");
                            }
                            System.out.println("\nPresiona Enter para continuar...");
                            scanner.nextLine();
                            break;

                        case 5: 
                            limpiarPantalla(); // <--- LIMPIEZA EXTRA
                            if (carrito.isEmpty()) {
                                System.out.println("Tu carrito está vacío.");
                            } else {
                                System.out.println("\n--- MODIFICAR CARRITO ---");
                                // Mostramos carrito actual para referencia
                                for (Producto p : carrito) {
                                    System.out.println("ID " + p.getId() + ": " + p.getNombre() + " (Llevas: " + p.getStock() + ")");
                                }
                                System.out.println("-------------------------");

                                System.out.print("Ingresa el ID del producto a modificar: ");
                                int idMod = scanner.nextInt();
                                scanner.nextLine();
                                
                                Producto itemEnCarrito = null;
                                for (Producto p : carrito) {
                                    if (p.getId() == idMod) {
                                        itemEnCarrito = p;
                                        break;
                                    }
                                }
                                
                                if (itemEnCarrito != null) {
                                    int stockMaximo = 0;
                                    for (Producto pCat : catalogo) {
                                        if (pCat.getId() == idMod) {
                                            stockMaximo = pCat.getStock();
                                            break;
                                        }
                                    }
                                    System.out.println("Stock máximo disponible: " + stockMaximo);
                                    System.out.print("Ingresa nueva cantidad (0 para eliminar): ");
                                    int nuevaCant = scanner.nextInt();
                                    scanner.nextLine();
                                    
                                    if (nuevaCant >= 0 && nuevaCant <= stockMaximo) {
                                        salida.writeObject("MODIFICAR");
                                        salida.writeObject(idMod);
                                        salida.writeObject(nuevaCant);
                                        
                                        if (nuevaCant == 0) {
                                            carrito.remove(itemEnCarrito);
                                            System.out.println("Producto eliminado.");
                                        } else {
                                            itemEnCarrito.setStock(nuevaCant);
                                            System.out.println("Cantidad modificada exitosamente.");
                                        }
                                    } else {
                                        System.out.println("Error: Cantidad no válida.");
                                    }
                                } else {
                                    System.out.println("Error: Ese producto no está en tu carrito.");
                                }
                            }
                            System.out.println("\nPresiona Enter para continuar...");
                            scanner.nextLine();
                            break;

                        case 0: 
                            System.out.println("¡Vuelva pronto!");
                            salida.writeObject("SALIR");
                            continuar = false;
                            
                            File carpeta = new File("Destino_CLT");
                            if (carpeta.exists() && carpeta.isDirectory()) {
                                File[] archivos = carpeta.listFiles();
                                if (archivos != null) {
                                    for (File f : archivos) {
                                        if (!f.getName().toLowerCase().endsWith(".pdf")) {
                                            f.delete();
                                        }
                                    }
                                }
                            }
                            break;

                        default:
                            System.out.println("Opción no válida.");
                            System.out.println("\nPresiona Enter para continuar...");
                            scanner.nextLine();
                    }
                } else {
                    System.out.println("Por favor, ingresa un número válido.");
                    scanner.nextLine(); 
                }
            }

            socket.close();
            scanner.close();

        } catch (Exception e) {
            System.out.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void limpiarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void generarReciboPDF(ArrayList<Producto> carrito, double total) {
        try {
            Document documento = new Document();
            PdfWriter.getInstance(documento, new FileOutputStream("Destino_CLT/Recibo_Compra.pdf"));
            documento.open();
            
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font fuenteNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            Paragraph titulo = new Paragraph("TIENDITA DE LA ESQUINA", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            
            Paragraph subtitulo = new Paragraph("Recibo de Compra\n", fuenteNormal);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(subtitulo);
            
            SimpleDateFormat formatoFecha = new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy HH:mm:ss", new Locale("es", "MX"));
            String fechaActual = formatoFecha.format(new Date());
            fechaActual = fechaActual.substring(0, 1).toUpperCase() + fechaActual.substring(1);
            
            Paragraph fecha = new Paragraph("Fecha: " + fechaActual + "\n\n", fuenteNormal);
            fecha.setAlignment(Element.ALIGN_RIGHT);
            documento.add(fecha);

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{1, 4, 2, 2}); 

            String[] headers = {"Cant.", "Producto", "P. Unit", "Subtotal"};
            for (String h : headers) {
                PdfPCell celda = new PdfPCell(new Phrase(h, fuenteNegrita));
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setBackgroundColor(BaseColor.LIGHT_GRAY);
                celda.setPadding(5);
                tabla.addCell(celda);
            }

            for (Producto p : carrito) {
                double subtotal = p.getPrecio() * p.getStock();
                
                PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(p.getStock()), fuenteNormal));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(c1);
                
                PdfPCell c2 = new PdfPCell(new Phrase(p.getNombre(), fuenteNormal));
                c2.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabla.addCell(c2);
                
                PdfPCell c3 = new PdfPCell(new Phrase("$" + p.getPrecio(), fuenteNormal));
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabla.addCell(c3);
                
                PdfPCell c4 = new PdfPCell(new Phrase("$" + subtotal, fuenteNormal));
                c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabla.addCell(c4);
            }
            
            documento.add(tabla);

            Paragraph lineaTotal = new Paragraph("\nGRAN TOTAL: $" + total, fuenteTitulo);
            lineaTotal.setAlignment(Element.ALIGN_RIGHT);
            documento.add(lineaTotal);
            
            Paragraph despedida = new Paragraph("\n¡Gracias por su preferencia!", fuenteNormal);
            despedida.setAlignment(Element.ALIGN_CENTER);
            documento.add(despedida);
            
            documento.close();
            
        } catch (Exception e) {
            System.out.println("Error creando el PDF: " + e.getMessage());
        }
    }
}
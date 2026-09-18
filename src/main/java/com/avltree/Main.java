package com.avltree;

import com.avltree.controller.AVLTreeController;
import com.avltree.service.MongoDBConnection;
import com.avltree.util.EnvLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Punto de entrada de la aplicación del árbol AVL de personas, ordenado por DPI.
 *
 * <p>El nivel de log se configura en {@code src/main/resources/logback.xml}.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final int REINTENTOS_POR_DEFECTO = 5;
    private static final int ESPERA_POR_DEFECTO_MS = 2000;

    public static void main(String[] args) {
        configurarSalidaUtf8();

        System.out.println("=== SISTEMA AVL GENÉRICO ===");
        System.out.println("Gestión de Personas ordenadas por DPI");
        System.out.println("Iniciando aplicación...\n");

        MongoDBConnection conexion = MongoDBConnection.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(conexion::close, "cierre-mongodb"));

        try {
            if (!esperarConexionMongoDB(conexion)) {
                System.err.println("\n❌ No se pudo establecer conexión con MongoDB. La aplicación se cerrará.");
                imprimirAyudaDeConexion();
                System.exit(1);
            }

            new AVLTreeController().run();

        } catch (Exception e) {
            log.error("Error fatal en la aplicación", e);
            System.err.println("❌ Error fatal en la aplicación: " + e.getMessage());
            imprimirAyudaDeConexion();
            System.exit(1);
        } finally {
            conexion.close();
        }
    }

    /**
     * Fuerza UTF-8 en la salida de consola.
     *
     * <p>Sin esto, los acentos y los símbolos del menú se ven como interrogaciones
     * en cualquier consola cuya codificación por defecto no sea UTF-8, empezando
     * por {@code cmd.exe} en Windows.
     */
    private static void configurarSalidaUtf8() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true,
                    StandardCharsets.UTF_8.name()));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true,
                    StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 siempre está disponible; si no lo estuviera, se sigue con la
            // codificación por defecto en vez de impedir el arranque.
            log.warn("No se pudo configurar la salida en UTF-8: {}", e.getMessage());
        }
    }

    /**
     * Intenta conectar con MongoDB, esperando entre intento e intento.
     *
     * @return true si alguna vez logró conectar
     */
    private static boolean esperarConexionMongoDB(MongoDBConnection conexion) {
        int intentosMaximos = Math.max(1, EnvLoader.getEnvAsInt("MAX_CONNECTION_RETRIES", REINTENTOS_POR_DEFECTO));
        int esperaMs = Math.max(0, EnvLoader.getEnvAsInt("RETRY_INTERVAL", ESPERA_POR_DEFECTO_MS));

        System.out.println("🔄 Estableciendo conexión con MongoDB Atlas...");

        for (int intento = 1; intento <= intentosMaximos; intento++) {
            System.out.printf("   Intento %d/%d de conexión...%n", intento, intentosMaximos);

            if (conexion.isConnected()) {
                System.out.println("✅ ¡Conexión con MongoDB establecida exitosamente!");
                System.out.println("📡 Base de datos lista para usar\n");
                return true;
            }

            if (intento < intentosMaximos && esperaMs > 0) {
                System.out.printf("⏳ Esperando %.1f segundos antes del siguiente intento...%n", esperaMs / 1000.0);
                try {
                    Thread.sleep(esperaMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("Conexión interrumpida.");
                    return false;
                }
            }
        }

        return false;
    }

    private static void imprimirAyudaDeConexion() {
        System.err.println("\n🔧 Verifique:");
        System.err.println("   - Su conexión a internet");
        System.err.println("   - Que MONGODB_URI esté definida en el archivo .env o como variable de entorno");
        System.err.println("   - Que el cluster de MongoDB Atlas esté activo");
        System.err.println("   - Que su IP esté en la lista blanca de MongoDB Atlas");
    }
}

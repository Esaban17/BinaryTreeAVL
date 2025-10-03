package com.avltree;

import com.avltree.controller.AVLTreeController;
import com.avltree.service.MongoDBConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal que inicia la aplicación del Árbol AVL de Personas
 * El árbol está ordenado por DPI (Documento Personal de Identificación)
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Configurar logging para silenciar mensajes DEBUG de MongoDB
            configureLogging();
            
            System.out.println("=== SISTEMA AVL GENÉRICO ===");
            System.out.println("Gestión de Personas ordenadas por DPI");
            System.out.println("Iniciando aplicación...\n");
            
            // Verificar conexión con MongoDB antes de continuar
            if (waitForMongoDBConnection()) {
                // Crear y ejecutar el controlador principal
                AVLTreeController controller = new AVLTreeController();
                controller.run();
            } else {
                System.err.println("❌ No se pudo establecer conexión con MongoDB.");
                System.err.println("La aplicación se cerrará.");
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error fatal en la aplicación: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\nPor favor, verifique:");
            System.err.println("- Que las dependencias de MongoDB estén disponibles");
            System.err.println("- Que el archivo .env esté configurado correctamente");
            System.err.println("- Que tenga conexión a internet para MongoDB Atlas");
            System.exit(1);
        }
    }
    
    /**
     * Espera a que se establezca la conexión con MongoDB
     * @return true si la conexión es exitosa, false en caso contrario
     */
    private static boolean waitForMongoDBConnection() {
        System.out.println("🔄 Estableciendo conexión con MongoDB Atlas...");
        
        int maxAttempts = 5;
        int attemptDelay = 2000; // 2 segundos entre intentos
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.printf("   Intento %d/%d de conexión...\n", attempt, maxAttempts);
                
                MongoDBConnection mongoConnection = MongoDBConnection.getInstance();
                
                if (mongoConnection.isConnected()) {
                    System.out.println("✅ ¡Conexión con MongoDB establecida exitosamente!");
                    System.out.println("📡 Base de datos lista para usar");
                    System.out.println("🚀 Iniciando interfaz de usuario...\n");
                    return true;
                }
                
            } catch (Exception e) {
                System.err.printf("❌ Intento %d falló: %s\n", attempt, e.getMessage());
                
                if (attempt < maxAttempts) {
                    System.out.printf("⏳ Esperando %d segundos antes del siguiente intento...\n", 
                                    attemptDelay / 1000);
                    try {
                        Thread.sleep(attemptDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        
        System.err.println("\n❌ Se agotaron todos los intentos de conexión.");
        System.err.println("🔧 Verifique:");
        System.err.println("   - Su conexión a internet");
        System.err.println("   - Las credenciales en el archivo .env");
        System.err.println("   - Que el cluster de MongoDB Atlas esté activo");
        System.err.println("   - Los whitelist IPs en MongoDB Atlas");
        
        return false;
    }
    
    /**
     * Configura el sistema de logging para silenciar mensajes DEBUG de MongoDB
     */
    private static void configureLogging() {
        // Configurar propiedades del sistema para logging
        System.setProperty("org.slf4j.simpleLogger.log.org.mongodb.driver", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.org.mongodb.driver.cluster", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.org.mongodb.driver.connection", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.org.mongodb.driver.protocol", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.org.mongodb.driver.operation", "warn");
        
        // Silenciar logs de MongoDB Driver a nivel WARN usando java.util.logging
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARNING);
        
        Logger clusterLogger = Logger.getLogger("org.mongodb.driver.cluster");
        clusterLogger.setLevel(Level.WARNING);
        
        Logger connectionLogger = Logger.getLogger("org.mongodb.driver.connection");
        connectionLogger.setLevel(Level.WARNING);
        
        Logger protocolLogger = Logger.getLogger("org.mongodb.driver.protocol");
        protocolLogger.setLevel(Level.WARNING);
        
        Logger operationLogger = Logger.getLogger("org.mongodb.driver.operation");
        operationLogger.setLevel(Level.WARNING);
        
        // Configurar el logger raíz para evitar mensajes excesivos
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
    }
}
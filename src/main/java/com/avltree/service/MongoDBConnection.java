package com.avltree.service;

import com.avltree.util.EnvLoader;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Clase para manejar la conexión con MongoDB Atlas
 */
public class MongoDBConnection {
    private static MongoDBConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private boolean connected = false;
    
    private MongoDBConnection() {
        // No conectar automáticamente en el constructor
        // La conexión se hace de forma explícita
    }
    
    /**
     * Patrón Singleton para obtener la instancia única de la conexión
     */
    public static MongoDBConnection getInstance() {
        if (instance == null) {
            instance = new MongoDBConnection();
        }
        return instance;
    }
    
    /**
     * Establece la conexión con MongoDB Atlas
     */
    private void connect() {
        try {
            // Cargar variables de entorno
            EnvLoader.loadEnv();
            
            String mongoUri = EnvLoader.getEnv("MONGODB_URI");
            String databaseName = EnvLoader.getEnv("DATABASE_NAME", "avltree");
            String collectionName = EnvLoader.getEnv("COLLECTION_NAME", "nodes");
            
            if (mongoUri == null) {
                throw new RuntimeException("MONGODB_URI no está definida en el archivo .env");
            }
            
            // Crear conexión
            mongoClient = MongoClients.create(mongoUri);
            database = mongoClient.getDatabase(databaseName);
            collection = database.getCollection(collectionName);
            
            // Verificar conexión
            database.runCommand(new Document("ping", 1));
            connected = true;
            
            System.out.println("✓ Conexión exitosa con MongoDB Atlas");
            System.out.println("✓ Base de datos: " + databaseName);
            System.out.println("✓ Colección: " + collectionName);
            
        } catch (Exception e) {
            connected = false;
            System.err.println("❌ Error al conectar con MongoDB: " + e.getMessage());
            System.err.println("Verifique las credenciales en el archivo .env");
            throw new RuntimeException("No se pudo establecer conexión con MongoDB", e);
        }
    }
    
    /**
     * Obtiene la colección de MongoDB
     */
    public MongoCollection<Document> getCollection() {
        return collection;
    }
    
    /**
     * Obtiene la base de datos de MongoDB
     */
    public MongoDatabase getDatabase() {
        return database;
    }
    
    /**
     * Obtiene el cliente de MongoDB
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }
    
    /**
     * Verifica si la conexión está activa
     */
    public boolean isConnected() {
        try {
            // Si no se ha intentado conectar, hacerlo ahora
            if (!connected && database == null) {
                connect();
            }
            
            if (database != null) {
                database.runCommand(new Document("ping", 1));
                connected = true;
                return true;
            }
        } catch (Exception e) {
            connected = false;
            System.err.println("Conexión perdida con MongoDB: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Reconecta a MongoDB en caso de pérdida de conexión
     */
    public void reconnect() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
            }
            connect();
        } catch (Exception e) {
            System.err.println("Error al reconectar con MongoDB: " + e.getMessage());
            throw new RuntimeException("No se pudo reconectar con MongoDB", e);
        }
    }
    
    /**
     * Cierra la conexión con MongoDB
     */
    public void close() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                System.out.println("Conexión con MongoDB cerrada correctamente");
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar la conexión con MongoDB: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene estadísticas de la conexión
     */
    public void printConnectionStats() {
        try {
            if (database != null) {
                Document stats = database.runCommand(new Document("dbStats", 1));
                System.out.println("\n=== ESTADÍSTICAS DE LA BASE DE DATOS ===");
                System.out.println("Nombre: " + database.getName());
                System.out.println("Colecciones: " + stats.getInteger("collections"));
                System.out.println("Documentos: " + collection.countDocuments());
                System.out.println("Tamaño de datos: " + stats.get("dataSize") + " bytes");
            }
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        }
    }
}
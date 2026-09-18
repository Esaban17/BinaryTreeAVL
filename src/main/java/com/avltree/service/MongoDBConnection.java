package com.avltree.service;

import com.avltree.util.EnvLoader;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Conexión con MongoDB Atlas.
 *
 * <p>Singleton implementado con el modismo del holder estático, que el
 * cargador de clases hace seguro entre hilos sin sincronización explícita.
 */
public class MongoDBConnection {

    private static final Logger log = LoggerFactory.getLogger(MongoDBConnection.class);

    private static final int TIMEOUT_POR_DEFECTO_MS = 10_000;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private volatile boolean connected;

    private MongoDBConnection() {
        // La conexión se establece de forma explícita, no en el constructor.
    }

    private static final class Holder {
        private static final MongoDBConnection INSTANCE = new MongoDBConnection();
    }

    public static MongoDBConnection getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Establece la conexión con MongoDB Atlas.
     *
     * @throws IllegalStateException si falta configuración o el servidor no responde
     */
    private synchronized void connect() {
        String mongoUri = EnvLoader.getEnv("MONGODB_URI");
        if (mongoUri == null || mongoUri.trim().isEmpty()) {
            throw new IllegalStateException(
                    "MONGODB_URI no está definida. Configúrala en el archivo .env o como variable de entorno.");
        }

        String databaseName = EnvLoader.getEnv("DATABASE_NAME", "avltree");
        String collectionName = EnvLoader.getEnv("COLLECTION_NAME", "nodes");
        int connectTimeout = EnvLoader.getEnvAsInt("CONNECTION_TIMEOUT", TIMEOUT_POR_DEFECTO_MS);
        int socketTimeout = EnvLoader.getEnvAsInt("SOCKET_TIMEOUT", TIMEOUT_POR_DEFECTO_MS);

        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToSocketSettings(b -> b
                            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                            .readTimeout(socketTimeout, TimeUnit.MILLISECONDS))
                    .applyToClusterSettings(b -> b
                            .serverSelectionTimeout(connectTimeout, TimeUnit.MILLISECONDS))
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
            collection = database.getCollection(collectionName);

            database.runCommand(new Document("ping", 1));
            connected = true;

            log.info("Conexión establecida con MongoDB (base de datos '{}', colección '{}')",
                    databaseName, collectionName);

        } catch (Exception e) {
            connected = false;
            closeQuietly();
            throw new IllegalStateException("No se pudo establecer conexión con MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Intenta conectar si aún no hay conexión y verifica que el servidor responda.
     *
     * @return true si la conexión está viva
     */
    public synchronized boolean isConnected() {
        try {
            if (database == null) {
                connect();
            }
            database.runCommand(new Document("ping", 1));
            connected = true;
            return true;
        } catch (Exception e) {
            connected = false;
            log.warn("MongoDB no responde: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Motivo del último fallo de conexión, o null si nunca falló.
     */
    public synchronized String describeLastFailure() {
        return connected ? null : "sin conexión activa con MongoDB";
    }

    public synchronized MongoCollection<Document> getCollection() {
        if (collection == null) {
            connect();
        }
        return collection;
    }

    public synchronized MongoDatabase getDatabase() {
        if (database == null) {
            connect();
        }
        return database;
    }

    public synchronized MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Cierra la conexión actual y abre una nueva.
     */
    public synchronized void reconnect() {
        closeQuietly();
        connect();
    }

    /**
     * Cierra la conexión con MongoDB. Es idempotente.
     */
    public synchronized void close() {
        if (mongoClient != null) {
            closeQuietly();
            log.info("Conexión con MongoDB cerrada correctamente");
        }
    }

    private void closeQuietly() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception e) {
                log.warn("Error al cerrar el cliente de MongoDB: {}", e.getMessage());
            }
        }
        mongoClient = null;
        database = null;
        collection = null;
        connected = false;
    }

    /**
     * Estadísticas de la base de datos, o null si no se pudieron obtener.
     */
    public synchronized Document getDatabaseStats() {
        try {
            if (database == null) {
                return null;
            }
            return database.runCommand(new Document("dbStats", 1));
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de la base de datos", e);
            return null;
        }
    }

    /**
     * Nombre de la base de datos en uso, o null si aún no hay conexión.
     */
    public synchronized String getDatabaseName() {
        return (database == null) ? null : database.getName();
    }
}

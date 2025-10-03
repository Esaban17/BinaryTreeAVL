package com.avltree.service;

import com.avltree.model.Node;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio genérico para manejar la persistencia del árbol AVL en MongoDB
 * @param <T> tipo de dato que debe implementar Comparable
 */
public class TreePersistenceService<T extends Comparable<T>> {
    private MongoDBConnection mongoConnection;
    private MongoCollection<Document> collection;
    private AVLTree<T> tree;
    private Class<T> clazz;
    
    public TreePersistenceService(AVLTree<T> tree, Class<T> clazz) {
        this.tree = tree;
        this.clazz = clazz;
        this.mongoConnection = MongoDBConnection.getInstance();
        this.collection = mongoConnection.getCollection();
    }
    
    /**
     * Guarda todo el árbol en MongoDB
     */
    public boolean saveTree() {
        try {
            // Limpiar la colección antes de guardar
            collection.deleteMany(new Document());
            
            if (tree.getRoot() == null) {
                System.out.println("El árbol está vacío, no hay nada que guardar");
                return true;
            }
            
            // Convertir árbol a lista de documentos
            List<Document> documents = new ArrayList<>();
            collectNodes(tree.getRoot(), documents);
            
            // Insertar documentos en MongoDB
            if (!documents.isEmpty()) {
                collection.insertMany(documents);
                System.out.println("✓ Árbol guardado exitosamente en MongoDB");
                System.out.println("✓ Nodos guardados: " + documents.size());
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Error al guardar el árbol: " + e.getMessage());
            return false;
        }
        return false;
    }
    
    /**
     * Carga el árbol desde MongoDB
     */
    public boolean loadTree() {
        try {
            // Limpiar árbol actual
            tree.setRoot(null);
            
            // Obtener todos los documentos de la colección
            MongoCursor<Document> cursor = collection.find().iterator();
            int loadedNodes = 0;
            
            try {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    Node<T> node = Node.fromDocument(doc, clazz);
                    if (node != null) {
                        tree.insert(node.getData());
                        loadedNodes++;
                    }
                }
            } finally {
                cursor.close();
            }
            
            if (loadedNodes > 0) {
                System.out.println("✓ Árbol cargado exitosamente desde MongoDB");
                System.out.println("✓ Nodos cargados: " + loadedNodes);
                return true;
            } else {
                System.out.println("No se encontraron datos en MongoDB");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar el árbol: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Guarda un nodo específico en MongoDB
     */
    public boolean saveNode(Node<T> node) {
        try {
            Document doc = node.toDocument();
            
            // Crear un identificador único basado en el objeto
            String identifier = generateIdentifier(node.getData());
            doc.append("_id", identifier);
            
            // Verificar si el nodo ya existe
            Document existingNode = collection.find(eq("_id", identifier)).first();
            
            if (existingNode != null) {
                // Actualizar nodo existente
                collection.replaceOne(eq("_id", identifier), doc);
                System.out.println("✓ Nodo actualizado en MongoDB: " + identifier);
            } else {
                // Insertar nuevo nodo
                collection.insertOne(doc);
                System.out.println("✓ Nodo guardado en MongoDB: " + identifier);
            }
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al guardar el nodo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina un nodo específico de MongoDB
     */
    public boolean deleteNode(T data) {
        try {
            String identifier = generateIdentifier(data);
            long deletedCount = collection.deleteOne(eq("_id", identifier)).getDeletedCount();
            
            if (deletedCount > 0) {
                System.out.println("✓ Nodo eliminado de MongoDB: " + identifier);
                return true;
            } else {
                System.out.println("⚠ No se encontró el nodo en MongoDB: " + identifier);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar el nodo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Busca un nodo en MongoDB
     */
    public Node<T> findNodeInDB(T data) {
        try {
            String identifier = generateIdentifier(data);
            Document doc = collection.find(eq("_id", identifier)).first();
            
            if (doc != null) {
                return Node.fromDocument(doc, clazz);
            } else {
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al buscar el nodo: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Sincroniza el árbol en memoria con MongoDB
     */
    public boolean syncWithDatabase() {
        try {
            System.out.println("Sincronizando árbol con base de datos...");
            
            // Primero cargar desde la base de datos
            boolean loaded = loadTree();
            
            if (loaded) {
                System.out.println("✓ Sincronización completada");
                return true;
            } else {
                System.out.println("⚠ No había datos para sincronizar");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error durante la sincronización: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene estadísticas de la base de datos
     */
    public void printDatabaseStats() {
        try {
            long totalNodes = collection.countDocuments();
            System.out.println("\n=== ESTADÍSTICAS DE LA BASE DE DATOS ===");
            System.out.println("Total de nodos en MongoDB: " + totalNodes);
            
            mongoConnection.printConnectionStats();
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener estadísticas: " + e.getMessage());
        }
    }
    
    /**
     * Elimina todos los datos de la base de datos
     */
    public boolean clearDatabase() {
        try {
            long deletedCount = collection.deleteMany(new Document()).getDeletedCount();
            System.out.println("✓ Base de datos limpiada. Documentos eliminados: " + deletedCount);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error al limpiar la base de datos: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Recolecta todos los nodos del árbol en una lista de documentos
     */
    private void collectNodes(Node<T> node, List<Document> documents) {
        if (node != null) {
            Document doc = node.toDocument();
            // Agregar identificador único
            doc.append("_id", generateIdentifier(node.getData()));
            documents.add(doc);
            collectNodes(node.getLeft(), documents);
            collectNodes(node.getRight(), documents);
        }
    }
    
    /**
     * Genera un identificador único para un objeto
     */
    private String generateIdentifier(T data) {
        if (data == null) {
            return "null";
        }
        // Usar el toString del objeto o su hashCode como identificador
        return data.getClass().getSimpleName() + "_" + Math.abs(data.hashCode());
    }
    
    /**
     * Verifica la integridad de los datos entre el árbol y la base de datos
     */
    public boolean verifyIntegrity() {
        try {
            List<Node<T>> treeNodes = tree.inorderTraversal();
            long dbNodeCount = collection.countDocuments();
            
            System.out.println("\n=== VERIFICACIÓN DE INTEGRIDAD ===");
            System.out.println("Nodos en árbol: " + treeNodes.size());
            System.out.println("Nodos en base de datos: " + dbNodeCount);
            
            if (treeNodes.size() != dbNodeCount) {
                System.out.println("⚠ Inconsistencia detectada en el número de nodos");
                return false;
            }
            
            // Verificar que cada nodo del árbol existe en la base de datos
            for (Node<T> node : treeNodes) {
                String identifier = generateIdentifier(node.getData());
                Document dbNode = collection.find(eq("_id", identifier)).first();
                if (dbNode == null) {
                    System.out.println("⚠ Nodo " + identifier + " no encontrado en base de datos");
                    return false;
                }
            }
            
            System.out.println("✓ Integridad verificada correctamente");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error durante la verificación: " + e.getMessage());
            return false;
        }
    }
}
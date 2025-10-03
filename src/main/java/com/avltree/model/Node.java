package com.avltree.model;

import org.bson.Document;

/**
 * Clase genérica que representa un nodo del árbol AVL
 * @param <T> tipo de dato que debe implementar Comparable
 */
public class Node<T extends Comparable<T>> {
    private T data;
    private int height;
    private Node<T> left;
    private Node<T> right;
    
    public Node(T data) {
        this.data = data;
        this.height = 1;
        this.left = null;
        this.right = null;
    }
    
    // Getters y Setters
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public Node<T> getLeft() {
        return left;
    }
    
    public void setLeft(Node<T> left) {
        this.left = left;
    }
    
    public Node<T> getRight() {
        return right;
    }
    
    public void setRight(Node<T> right) {
        this.right = right;
    }
    
    /**
     * Compara este nodo con otro usando el método compareTo del tipo T
     */
    public int compareTo(Node<T> other) {
        if (other == null || other.data == null) {
            return this.data == null ? 0 : 1;
        }
        if (this.data == null) {
            return -1;
        }
        return this.data.compareTo(other.data);
    }
    
    /**
     * Compara el dato de este nodo con un dato externo
     */
    public int compareToData(T otherData) {
        if (otherData == null) {
            return this.data == null ? 0 : 1;
        }
        if (this.data == null) {
            return -1;
        }
        return this.data.compareTo(otherData);
    }
    
    /**
     * Convierte el nodo a un documento de MongoDB
     * Nota: Solo funciona si T tiene un método toDocument() o es serializable
     */
    public Document toDocument() {
        Document doc = new Document()
                .append("height", height);
        
        // Si el tipo T tiene método toDocument, lo usamos
        if (data != null) {
            try {
                // Intentar usar el método toDocument si existe
                java.lang.reflect.Method toDocMethod = data.getClass().getMethod("toDocument");
                Document dataDoc = (Document) toDocMethod.invoke(data);
                doc.append("data", dataDoc);
            } catch (Exception e) {
                // Si no tiene toDocument, guardamos como string
                doc.append("data", data.toString());
            }
        }
        
        return doc;
    }
    
    /**
     * Crea un nodo desde un documento de MongoDB
     * Nota: Requiere una implementación específica para cada tipo T
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Node<T> fromDocument(Document doc, Class<T> clazz) {
        if (doc == null) {
            return null;
        }
        
        try {
            // Intentar usar el método fromDocument estático si existe
            java.lang.reflect.Method fromDocMethod = clazz.getMethod("fromDocument", Document.class);
            Object dataDoc = doc.get("data");
            T data = null;
            
            if (dataDoc instanceof Document) {
                data = (T) fromDocMethod.invoke(null, dataDoc);
            }
            
            if (data != null) {
                Node<T> node = new Node<>(data);
                node.setHeight(doc.getInteger("height", 1));
                return node;
            }
        } catch (Exception e) {
            // Manejo de error en caso de que no se pueda deserializar
            System.err.println("Error al deserializar nodo: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return "Node{" +
                "data=" + data +
                ", height=" + height +
                '}';
    }
    
    /**
     * Representación compacta del nodo para visualización
     */
    public String toDisplayString() {
        if (data != null) {
            // Si el tipo T tiene método toShortString, lo usamos
            try {
                java.lang.reflect.Method toShortMethod = data.getClass().getMethod("toShortString");
                return (String) toShortMethod.invoke(data) + " (h:" + height + ")";
            } catch (Exception e) {
                // Si no tiene toShortString, usamos toString
                return data.toString() + " (h:" + height + ")";
            }
        }
        return "null (h:" + height + ")";
    }
}
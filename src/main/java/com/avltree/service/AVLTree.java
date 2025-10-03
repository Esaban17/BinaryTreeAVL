package com.avltree.service;

import com.avltree.model.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación genérica de un árbol AVL (Árbol Balanceado de Adelson-Velsky y Landis)
 * @param <T> tipo de dato que debe implementar Comparable
 */
public class AVLTree<T extends Comparable<T>> {
    private Node<T> root;
    
    public AVLTree() {
        this.root = null;
    }
    
    public Node<T> getRoot() {
        return root;
    }
    
    public void setRoot(Node<T> root) {
        this.root = root;
    }
    
    /**
     * Obtiene la altura de un nodo
     */
    private int getHeight(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return node.getHeight();
    }
    
    /**
     * Calcula el factor de balance de un nodo
     */
    private int getBalance(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return getHeight(node.getLeft()) - getHeight(node.getRight());
    }
    
    /**
     * Actualiza la altura de un nodo
     */
    private void updateHeight(Node<T> node) {
        if (node != null) {
            node.setHeight(1 + Math.max(getHeight(node.getLeft()), getHeight(node.getRight())));
        }
    }
    
    /**
     * Rotación a la derecha
     */
    private Node<T> rotateRight(Node<T> y) {
        Node<T> x = y.getLeft();
        Node<T> T2 = x.getRight();
        
        // Realizar rotación
        x.setRight(y);
        y.setLeft(T2);
        
        // Actualizar alturas
        updateHeight(y);
        updateHeight(x);
        
        return x;
    }
    
    /**
     * Rotación a la izquierda
     */
    private Node<T> rotateLeft(Node<T> x) {
        Node<T> y = x.getRight();
        Node<T> T2 = y.getLeft();
        
        // Realizar rotación
        y.setLeft(x);
        x.setRight(T2);
        
        // Actualizar alturas
        updateHeight(x);
        updateHeight(y);
        
        return y;
    }
    
    /**
     * Inserta un nuevo elemento en el árbol
     */
    public void insert(T data) {
        root = insertNode(root, data);
    }
    
    private Node<T> insertNode(Node<T> node, T data) {
        // 1. Inserción normal de BST
        if (node == null) {
            return new Node<>(data);
        }
        
        int comparison = data.compareTo(node.getData());
        
        if (comparison < 0) {
            node.setLeft(insertNode(node.getLeft(), data));
        } else if (comparison > 0) {
            node.setRight(insertNode(node.getRight(), data));
        } else {
            // Los datos duplicados no están permitidos (actualizar)
            node.setData(data);
            return node;
        }
        
        // 2. Actualizar altura del nodo ancestro
        updateHeight(node);
        
        // 3. Obtener el factor de balance
        int balance = getBalance(node);
        
        // 4. Si el nodo está desbalanceado, hay 4 casos
        
        // Caso Izquierda Izquierda
        if (balance > 1 && data.compareTo(node.getLeft().getData()) < 0) {
            return rotateRight(node);
        }
        
        // Caso Derecha Derecha
        if (balance < -1 && data.compareTo(node.getRight().getData()) > 0) {
            return rotateLeft(node);
        }
        
        // Caso Izquierda Derecha
        if (balance > 1 && data.compareTo(node.getLeft().getData()) > 0) {
            node.setLeft(rotateLeft(node.getLeft()));
            return rotateRight(node);
        }
        
        // Caso Derecha Izquierda
        if (balance < -1 && data.compareTo(node.getRight().getData()) < 0) {
            node.setRight(rotateRight(node.getRight()));
            return rotateLeft(node);
        }
        
        return node;
    }
    
    /**
     * Busca un elemento en el árbol
     */
    public Node<T> search(T data) {
        return searchNode(root, data);
    }
    
    private Node<T> searchNode(Node<T> node, T data) {
        if (node == null || data.compareTo(node.getData()) == 0) {
            return node;
        }
        
        if (data.compareTo(node.getData()) < 0) {
            return searchNode(node.getLeft(), data);
        } else {
            return searchNode(node.getRight(), data);
        }
    }
    
    /**
     * Actualiza un elemento en el árbol
     */
    public boolean update(T oldData, T newData) {
        Node<T> node = search(oldData);
        if (node != null) {
            // Si la clave de comparación es la misma, solo actualizar
            if (oldData.compareTo(newData) == 0) {
                node.setData(newData);
                return true;
            } else {
                // Si la clave cambió, eliminar el viejo e insertar el nuevo
                delete(oldData);
                insert(newData);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Elimina un elemento del árbol
     */
    public void delete(T data) {
        root = deleteNode(root, data);
    }
    
    private Node<T> deleteNode(Node<T> root, T data) {
        // 1. Eliminación normal de BST
        if (root == null) {
            return root;
        }
        
        int comparison = data.compareTo(root.getData());
        
        if (comparison < 0) {
            root.setLeft(deleteNode(root.getLeft(), data));
        } else if (comparison > 0) {
            root.setRight(deleteNode(root.getRight(), data));
        } else {
            // Nodo con una sola hoja o sin hijos
            if ((root.getLeft() == null) || (root.getRight() == null)) {
                Node<T> temp = null;
                if (temp == root.getLeft()) {
                    temp = root.getRight();
                } else {
                    temp = root.getLeft();
                }
                
                // Sin hijos
                if (temp == null) {
                    temp = root;
                    root = null;
                } else {
                    // Un hijo
                    root = temp;
                }
            } else {
                // Nodo con dos hijos: obtener el sucesor inorder
                Node<T> temp = minValueNode(root.getRight());
                
                // Copiar los datos del sucesor inorder a este nodo
                root.setData(temp.getData());
                
                // Eliminar el sucesor inorder
                root.setRight(deleteNode(root.getRight(), temp.getData()));
            }
        }
        
        // Si el árbol tenía solo un nodo, retornar
        if (root == null) {
            return root;
        }
        
        // 2. Actualizar altura del nodo actual
        updateHeight(root);
        
        // 3. Obtener el factor de balance
        int balance = getBalance(root);
        
        // 4. Si el nodo está desbalanceado, hay 4 casos
        
        // Caso Izquierda Izquierda
        if (balance > 1 && getBalance(root.getLeft()) >= 0) {
            return rotateRight(root);
        }
        
        // Caso Izquierda Derecha
        if (balance > 1 && getBalance(root.getLeft()) < 0) {
            root.setLeft(rotateLeft(root.getLeft()));
            return rotateRight(root);
        }
        
        // Caso Derecha Derecha
        if (balance < -1 && getBalance(root.getRight()) <= 0) {
            return rotateLeft(root);
        }
        
        // Caso Derecha Izquierda
        if (balance < -1 && getBalance(root.getRight()) > 0) {
            root.setRight(rotateRight(root.getRight()));
            return rotateLeft(root);
        }
        
        return root;
    }
    
    /**
     * Encuentra el nodo con el valor mínimo
     */
    private Node<T> minValueNode(Node<T> node) {
        Node<T> current = node;
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current;
    }
    
    /**
     * Recorrido inorder del árbol
     */
    public List<Node<T>> inorderTraversal() {
        List<Node<T>> result = new ArrayList<>();
        inorderTraversalHelper(root, result);
        return result;
    }
    
    private void inorderTraversalHelper(Node<T> node, List<Node<T>> result) {
        if (node != null) {
            inorderTraversalHelper(node.getLeft(), result);
            result.add(node);
            inorderTraversalHelper(node.getRight(), result);
        }
    }
    
    /**
     * Verifica si el árbol está vacío
     */
    public boolean isEmpty() {
        return root == null;
    }
    
    /**
     * Obtiene el número total de nodos en el árbol
     */
    public int size() {
        return sizeHelper(root);
    }
    
    private int sizeHelper(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return 1 + sizeHelper(node.getLeft()) + sizeHelper(node.getRight());
    }
}
package com.avltree.service;

import com.avltree.model.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación genérica de un árbol AVL (Adelson-Velsky y Landis).
 *
 * <p>El árbol se comporta como un conjunto ordenado: dos datos que comparan
 * igual ({@code compareTo == 0}) ocupan el mismo nodo.
 *
 * @param <T> tipo de dato que debe implementar Comparable
 */
public class AVLTree<T extends Comparable<T>> {

    /** Resultado de una operación {@link #update(Comparable, Comparable)}. */
    public enum UpdateResult {
        /** El dato se actualizó correctamente. */
        UPDATED,
        /** No existe ningún nodo que compare igual a {@code oldData}. */
        NOT_FOUND,
        /** La nueva clave ya pertenece a otro nodo del árbol. */
        KEY_CONFLICT
    }

    private Node<T> root;
    private int size;

    public AVLTree() {
        this.root = null;
        this.size = 0;
    }

    public Node<T> getRoot() {
        return root;
    }

    /**
     * Reemplaza la raíz del árbol. Recalcula el tamaño recorriendo la nueva
     * estructura, de modo que {@link #size()} siga siendo coherente.
     */
    public void setRoot(Node<T> root) {
        this.root = root;
        this.size = countNodes(root);
    }

    private int getHeight(Node<T> node) {
        return (node == null) ? 0 : node.getHeight();
    }

    private int getBalance(Node<T> node) {
        return (node == null) ? 0 : getHeight(node.getLeft()) - getHeight(node.getRight());
    }

    private void updateHeight(Node<T> node) {
        if (node != null) {
            node.setHeight(1 + Math.max(getHeight(node.getLeft()), getHeight(node.getRight())));
        }
    }

    private Node<T> rotateRight(Node<T> y) {
        Node<T> x = y.getLeft();
        Node<T> subarbolMedio = x.getRight();

        x.setRight(y);
        y.setLeft(subarbolMedio);

        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private Node<T> rotateLeft(Node<T> x) {
        Node<T> y = x.getRight();
        Node<T> subarbolMedio = y.getLeft();

        y.setLeft(x);
        x.setRight(subarbolMedio);

        updateHeight(x);
        updateHeight(y);
        return y;
    }

    /**
     * Actualiza la altura del nodo y aplica la rotación que corresponda.
     * Es el único punto del árbol que decide rotaciones, tanto en inserción
     * como en eliminación, y siempre en función del factor de balance.
     */
    private Node<T> rebalance(Node<T> node) {
        updateHeight(node);
        int balance = getBalance(node);

        if (balance > 1) {
            // Pesado a la izquierda: caso izquierda-derecha o izquierda-izquierda
            if (getBalance(node.getLeft()) < 0) {
                node.setLeft(rotateLeft(node.getLeft()));
            }
            return rotateRight(node);
        }

        if (balance < -1) {
            // Pesado a la derecha: caso derecha-izquierda o derecha-derecha
            if (getBalance(node.getRight()) > 0) {
                node.setRight(rotateRight(node.getRight()));
            }
            return rotateLeft(node);
        }

        return node;
    }

    /**
     * Inserta un dato en el árbol. Si ya existe un nodo con la misma clave,
     * ese nodo se actualiza en vez de crearse uno nuevo.
     *
     * @return true si se agregó un nodo nuevo, false si se actualizó uno existente
     */
    public boolean insert(T data) {
        requireData(data);
        int antes = size;
        root = insertNode(root, data);
        return size > antes;
    }

    private Node<T> insertNode(Node<T> node, T data) {
        if (node == null) {
            size++;
            return new Node<>(data);
        }

        int comparison = data.compareTo(node.getData());

        if (comparison < 0) {
            node.setLeft(insertNode(node.getLeft(), data));
        } else if (comparison > 0) {
            node.setRight(insertNode(node.getRight(), data));
        } else {
            // Clave duplicada: se actualiza el contenido, la estructura no cambia
            node.setData(data);
            return node;
        }

        return rebalance(node);
    }

    /**
     * Busca el nodo cuya clave compara igual al dato recibido.
     *
     * @return el nodo encontrado, o null si no existe
     */
    public Node<T> search(T data) {
        if (data == null) {
            return null;
        }
        Node<T> current = root;
        while (current != null) {
            int comparison = data.compareTo(current.getData());
            if (comparison == 0) {
                return current;
            }
            current = (comparison < 0) ? current.getLeft() : current.getRight();
        }
        return null;
    }

    /**
     * @return true si el árbol contiene un nodo con esa clave
     */
    public boolean contains(T data) {
        return search(data) != null;
    }

    /**
     * Actualiza el dato asociado a {@code oldData}.
     *
     * <p>Si la clave no cambia, se reemplaza el contenido del nodo. Si la clave
     * cambia, se elimina el nodo viejo y se inserta el nuevo, salvo que la nueva
     * clave ya pertenezca a otro nodo: en ese caso no se modifica nada y se
     * devuelve {@link UpdateResult#KEY_CONFLICT}, para no destruir ese registro.
     */
    public UpdateResult update(T oldData, T newData) {
        requireData(newData);
        Node<T> node = search(oldData);
        if (node == null) {
            return UpdateResult.NOT_FOUND;
        }

        if (oldData.compareTo(newData) == 0) {
            node.setData(newData);
            return UpdateResult.UPDATED;
        }

        if (contains(newData)) {
            return UpdateResult.KEY_CONFLICT;
        }

        delete(oldData);
        insert(newData);
        return UpdateResult.UPDATED;
    }

    /**
     * Elimina del árbol el nodo cuya clave compara igual al dato recibido.
     *
     * @return true si se eliminó un nodo, false si no existía
     */
    public boolean delete(T data) {
        if (data == null) {
            return false;
        }
        int antes = size;
        root = deleteNode(root, data);
        return size < antes;
    }

    private Node<T> deleteNode(Node<T> node, T data) {
        if (node == null) {
            return null;
        }

        int comparison = data.compareTo(node.getData());

        if (comparison < 0) {
            node.setLeft(deleteNode(node.getLeft(), data));
        } else if (comparison > 0) {
            node.setRight(deleteNode(node.getRight(), data));
        } else if (node.getLeft() == null || node.getRight() == null) {
            // Cero o un hijo: el nodo se sustituye por ese hijo (o desaparece)
            size--;
            Node<T> hijo = (node.getLeft() != null) ? node.getLeft() : node.getRight();
            if (hijo == null) {
                return null;
            }
            node = hijo;
        } else {
            // Dos hijos: se copia el sucesor inorder y se elimina el sucesor
            Node<T> sucesor = minValueNode(node.getRight());
            node.setData(sucesor.getData());
            node.setRight(deleteNode(node.getRight(), sucesor.getData()));
        }

        return rebalance(node);
    }

    /**
     * Encuentra el nodo con el valor mínimo del subárbol.
     */
    private Node<T> minValueNode(Node<T> node) {
        Node<T> current = node;
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current;
    }

    /**
     * Recorrido inorder: devuelve los nodos ordenados por clave.
     */
    public List<Node<T>> inorderTraversal() {
        List<Node<T>> result = new ArrayList<>(size);
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
     * Devuelve los datos ordenados por clave.
     */
    public List<T> toSortedList() {
        List<T> result = new ArrayList<>(size);
        for (Node<T> node : inorderTraversal()) {
            result.add(node.getData());
        }
        return result;
    }

    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Número total de nodos. O(1): se mantiene incrementalmente.
     */
    public int size() {
        return size;
    }

    /**
     * Altura del árbol; 0 si está vacío.
     */
    public int height() {
        return getHeight(root);
    }

    /**
     * Elimina todos los nodos del árbol.
     */
    public void clear() {
        root = null;
        size = 0;
    }

    private int countNodes(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.getLeft()) + countNodes(node.getRight());
    }

    private void requireData(T data) {
        if (data == null) {
            throw new IllegalArgumentException("El árbol AVL no admite datos null");
        }
    }
}

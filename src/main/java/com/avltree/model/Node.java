package com.avltree.model;

import java.util.function.Function;

/**
 * Nodo genérico de un árbol AVL.
 *
 * <p>Esta clase es puramente una estructura de datos: no conoce MongoDB ni ningún
 * otro mecanismo de almacenamiento. La conversión entre objetos de dominio y
 * documentos es responsabilidad de {@code com.avltree.service.DocumentMapper}.
 *
 * @param <T> tipo de dato que debe implementar Comparable
 */
public class Node<T extends Comparable<T>> {
    private T data;
    private int height;
    private Node<T> left;
    private Node<T> right;

    /**
     * @param data dato del nodo; no puede ser null
     */
    public Node(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Un nodo del árbol no puede contener datos null");
        }
        this.data = data;
        this.height = 1;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Un nodo del árbol no puede contener datos null");
        }
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
     * Compara este nodo con otro usando el compareTo del tipo T.
     */
    public int compareTo(Node<T> other) {
        if (other == null) {
            return 1;
        }
        return this.data.compareTo(other.data);
    }

    /**
     * Compara el dato de este nodo con un dato externo.
     */
    public int compareToData(T otherData) {
        if (otherData == null) {
            return 1;
        }
        return this.data.compareTo(otherData);
    }

    @Override
    public String toString() {
        return "Node{data=" + data + ", height=" + height + '}';
    }

    /**
     * Representación compacta del nodo usando {@code toString()} del dato.
     */
    public String toDisplayString() {
        return toDisplayString(String::valueOf);
    }

    /**
     * Representación compacta del nodo usando un formateador explícito.
     *
     * @param formatter cómo mostrar el dato; si es null se usa {@code toString()}
     */
    public String toDisplayString(Function<? super T, String> formatter) {
        String texto = (formatter == null) ? String.valueOf(data) : formatter.apply(data);
        return texto + " (h:" + height + ")";
    }
}

package com.avltree.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    @Test
    void unNodoNuevoEsUnaHojaDeAlturaUno() {
        Node<Integer> node = new Node<>(10);

        assertEquals(10, node.getData());
        assertEquals(1, node.getHeight());
        assertNull(node.getLeft());
        assertNull(node.getRight());
    }

    @Test
    void rechazaDatosNull() {
        assertThrows(IllegalArgumentException.class, () -> new Node<Integer>(null));
        assertThrows(IllegalArgumentException.class, () -> new Node<>(10).setData(null));
    }

    @Test
    void comparaConOtroNodo() {
        Node<Integer> diez = new Node<>(10);
        Node<Integer> veinte = new Node<>(20);

        assertTrue(diez.compareTo(veinte) < 0);
        assertTrue(veinte.compareTo(diez) > 0);
        assertEquals(0, diez.compareTo(new Node<>(10)));
        assertTrue(diez.compareTo(null) > 0);
    }

    @Test
    void comparaConUnDatoExterno() {
        Node<Integer> diez = new Node<>(10);

        assertTrue(diez.compareToData(20) < 0);
        assertEquals(0, diez.compareToData(10));
        assertTrue(diez.compareToData(null) > 0);
    }

    @Test
    void laRepresentacionCompactaIncluyeLaAltura() {
        Node<Persona> node = new Node<>(new Persona("Ana", "López", 30, "12345678"));
        node.setHeight(3);

        assertEquals("Ana López (12345678) (h:3)", node.toDisplayString(Persona::toShortString));
        assertTrue(node.toDisplayString().contains("(h:3)"));
    }
}

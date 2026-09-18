package com.avltree.util;

import com.avltree.model.Node;
import com.avltree.model.Persona;
import com.avltree.service.AVLTree;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TreeVisualizerTest {

    private final PrintStream salidaOriginal = System.out;
    private ByteArrayOutputStream salidaCapturada;

    @BeforeEach
    void capturarSalida() {
        salidaCapturada = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salidaCapturada, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(salidaOriginal);
    }

    private String salida() {
        return new String(salidaCapturada.toByteArray(), StandardCharsets.UTF_8);
    }

    private AVLTree<Integer> arbolDe(Integer... valores) {
        AVLTree<Integer> arbol = new AVLTree<>();
        Arrays.asList(valores).forEach(arbol::insert);
        return arbol;
    }

    @Test
    void detectaUnArbolBalanceado() {
        assertTrue(TreeVisualizer.isBalanced(arbolDe(10, 20, 30, 40, 50).getRoot()));
        assertTrue(TreeVisualizer.isBalanced(null), "Un árbol vacío está balanceado");
    }

    @Test
    @DisplayName("Detecta un desbalance construido a mano")
    void detectaUnArbolDesbalanceado() {
        Node<Integer> raiz = new Node<>(30);
        raiz.setLeft(new Node<>(20));
        raiz.getLeft().setLeft(new Node<>(10));
        raiz.getLeft().setHeight(2);
        raiz.setHeight(3);

        assertFalse(TreeVisualizer.isBalanced(raiz));
    }

    @Test
    @DisplayName("Detecta alturas almacenadas incoherentes con la estructura")
    void detectaAlturasIncorrectas() {
        Node<Integer> raiz = new Node<>(20);
        raiz.setLeft(new Node<>(10));
        raiz.setRight(new Node<>(30));
        raiz.setHeight(99);

        assertFalse(TreeVisualizer.isBalanced(raiz));
    }

    @Test
    void cuentaLosNodos() {
        assertEquals(0, TreeVisualizer.countNodes(null));
        assertEquals(5, TreeVisualizer.countNodes(arbolDe(10, 20, 30, 40, 50).getRoot()));
    }

    @Test
    void elRecorridoInordenSeImprimeOrdenado() {
        TreeVisualizer.printSimpleTree(arbolDe(30, 10, 20).getRoot(), String::valueOf);

        String texto = salida();
        assertTrue(texto.indexOf("[10") < texto.indexOf("[20"), texto);
        assertTrue(texto.indexOf("[20") < texto.indexOf("[30"), texto);
    }

    @Test
    @DisplayName("La estructura jerárquica muestra todos los nodos y marca las ramas")
    void laEstructuraJerarquicaIncluyeTodosLosNodos() {
        TreeVisualizer.printHierarchicalTree(arbolDe(50, 30, 70, 20, 40).getRoot(), String::valueOf);

        String texto = salida();
        for (String valor : Arrays.asList("20", "30", "40", "50", "70")) {
            assertTrue(texto.contains("[" + valor + " "), "Falta el nodo " + valor + " en:\n" + texto);
        }
        assertTrue(texto.contains("L: "), "Debe marcar las ramas izquierdas");
        assertTrue(texto.contains("R: "), "Debe marcar las ramas derechas");
    }

    @Test
    void usaElFormateadorRecibido() {
        AVLTree<Persona> arbol = new AVLTree<>();
        arbol.insert(new Persona("Ana", "López", 30, "12345678"));

        TreeVisualizer.printSimpleTree(arbol.getRoot(), Persona::toShortString);

        assertTrue(salida().contains("Ana López (12345678)"), salida());
    }

    @Test
    void informaCuandoElArbolEstaVacio() {
        TreeVisualizer.printHierarchicalTree(null, String::valueOf);
        TreeVisualizer.printSimpleTree(null, String::valueOf);
        TreeVisualizer.printTreeInfo(null, String::valueOf);

        assertEquals(3, salida().split("El árbol está vacío", -1).length - 1);
    }

    @Test
    void laInformacionDelArbolIncluyeAlturaYBalance() {
        TreeVisualizer.printTreeInfo(arbolDe(50, 30, 70, 20).getRoot(), String::valueOf);

        String texto = salida();
        assertTrue(texto.contains("Altura del árbol: 3"), texto);
        assertTrue(texto.contains("Número de nodos: 4"), texto);
        assertTrue(texto.contains("Está balanceado: true"), texto);
        assertTrue(texto.contains("Nivel 1: 1 nodos"), texto);
    }

    @Test
    void rechazaOpcionesDeVisualizacionInvalidas() {
        Node<Integer> raiz = arbolDe(10).getRoot();

        assertTrue(TreeVisualizer.executeVisualization(raiz, 1, String::valueOf));
        assertTrue(TreeVisualizer.executeVisualization(raiz, 4, String::valueOf));
        assertFalse(TreeVisualizer.executeVisualization(raiz, 99, String::valueOf));
    }
}

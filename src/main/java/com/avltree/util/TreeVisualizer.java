package com.avltree.util;

import com.avltree.model.Node;
import java.util.function.Function;

/**
 * Visualización del árbol AVL en consola.
 */
public final class TreeVisualizer {

    private TreeVisualizer() {
    }

    /**
     * Imprime el árbol en formato jerárquico, marcando cada rama como
     * izquierda (L) o derecha (R).
     *
     * @param formatter cómo mostrar el dato de cada nodo
     */
    public static <T extends Comparable<T>> void printHierarchicalTree(
            Node<T> root, Function<? super T, String> formatter) {
        if (root == null) {
            System.out.println("El árbol está vacío");
            return;
        }

        System.out.println("\n=== ESTRUCTURA JERÁRQUICA ===");
        System.out.println("[" + root.toDisplayString(formatter) + "]");
        printChildren(root, "", formatter);
    }

    private static <T extends Comparable<T>> void printChildren(
            Node<T> node, String prefix, Function<? super T, String> formatter) {
        Node<T> left = node.getLeft();
        Node<T> right = node.getRight();

        if (left != null) {
            boolean esUltimo = (right == null);
            printBranch(left, prefix, esUltimo ? "└── L: " : "├── L: ", esUltimo, formatter);
        }
        if (right != null) {
            printBranch(right, prefix, "└── R: ", true, formatter);
        }
    }

    private static <T extends Comparable<T>> void printBranch(
            Node<T> node, String prefix, String conector, boolean esUltimo,
            Function<? super T, String> formatter) {
        System.out.println(prefix + conector + "[" + node.toDisplayString(formatter) + "]");
        printChildren(node, prefix + (esUltimo ? "    " : "│   "), formatter);
    }

    /**
     * Imprime el recorrido inorder en una sola línea.
     */
    public static <T extends Comparable<T>> void printSimpleTree(
            Node<T> root, Function<? super T, String> formatter) {
        if (root == null) {
            System.out.println("El árbol está vacío");
            return;
        }

        System.out.println("\n=== RECORRIDO INORDER ===");
        printInorder(root, formatter);
        System.out.println();
    }

    private static <T extends Comparable<T>> void printInorder(
            Node<T> node, Function<? super T, String> formatter) {
        if (node != null) {
            printInorder(node.getLeft(), formatter);
            System.out.print("[" + node.toDisplayString(formatter) + "] ");
            printInorder(node.getRight(), formatter);
        }
    }

    /**
     * Imprime altura, número de nodos, balance y distribución por nivel.
     */
    public static <T extends Comparable<T>> void printTreeInfo(
            Node<T> root, Function<? super T, String> formatter) {
        if (root == null) {
            System.out.println("El árbol está vacío");
            return;
        }

        System.out.println("\n=== INFORMACIÓN DEL ÁRBOL ===");
        System.out.println("Altura del árbol: " + getHeight(root));
        System.out.println("Número de nodos: " + countNodes(root));
        System.out.println("Nodo raíz: [" + root.toDisplayString(formatter) + "]");
        System.out.println("Está balanceado: " + isBalanced(root));

        System.out.println("\nNodos por nivel:");
        printLevelStats(root);
    }

    private static <T extends Comparable<T>> int getHeight(Node<T> node) {
        return (node == null) ? 0 : node.getHeight();
    }

    /**
     * Cuenta los nodos recorriendo la estructura. Sirve como verificación
     * independiente del contador que mantiene el árbol.
     */
    public static <T extends Comparable<T>> int countNodes(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.getLeft()) + countNodes(node.getRight());
    }

    /**
     * Verifica la propiedad AVL: factor de balance en [-1, 1] y alturas
     * almacenadas coherentes con la estructura real.
     */
    public static <T extends Comparable<T>> boolean isBalanced(Node<T> node) {
        return alturaSiEstaBalanceado(node) >= 0;
    }

    private static <T extends Comparable<T>> int alturaSiEstaBalanceado(Node<T> node) {
        if (node == null) {
            return 0;
        }

        int izquierda = alturaSiEstaBalanceado(node.getLeft());
        if (izquierda < 0) {
            return -1;
        }
        int derecha = alturaSiEstaBalanceado(node.getRight());
        if (derecha < 0) {
            return -1;
        }

        if (Math.abs(izquierda - derecha) > 1) {
            return -1;
        }

        int alturaReal = 1 + Math.max(izquierda, derecha);
        return (node.getHeight() == alturaReal) ? alturaReal : -1;
    }

    private static <T extends Comparable<T>> void printLevelStats(Node<T> root) {
        int height = getHeight(root);
        for (int nivel = 1; nivel <= height; nivel++) {
            System.out.println("Nivel " + nivel + ": " + countNodesAtLevel(root, nivel) + " nodos");
        }
    }

    private static <T extends Comparable<T>> int countNodesAtLevel(Node<T> node, int level) {
        if (node == null) {
            return 0;
        }
        if (level == 1) {
            return 1;
        }
        return countNodesAtLevel(node.getLeft(), level - 1)
                + countNodesAtLevel(node.getRight(), level - 1);
    }

    /**
     * Muestra el menú de opciones de visualización.
     */
    public static void showVisualizationMenu() {
        System.out.println("\n=== OPCIONES DE VISUALIZACIÓN ===");
        System.out.println("1. Estructura jerárquica");
        System.out.println("2. Recorrido inorder");
        System.out.println("3. Información detallada del árbol");
        System.out.println("4. Todas las visualizaciones");
        System.out.println("0. Regresar al menú principal");
    }

    /**
     * Ejecuta la visualización elegida.
     *
     * @return false si la opción no es válida
     */
    public static <T extends Comparable<T>> boolean executeVisualization(
            Node<T> root, int option, Function<? super T, String> formatter) {
        switch (option) {
            case 1:
                printHierarchicalTree(root, formatter);
                return true;
            case 2:
                printSimpleTree(root, formatter);
                return true;
            case 3:
                printTreeInfo(root, formatter);
                return true;
            case 4:
                printHierarchicalTree(root, formatter);
                printSimpleTree(root, formatter);
                printTreeInfo(root, formatter);
                return true;
            default:
                System.out.println("❌ Opción no válida");
                return false;
        }
    }
}

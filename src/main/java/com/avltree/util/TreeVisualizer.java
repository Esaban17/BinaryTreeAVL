package com.avltree.util;

import com.avltree.model.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilidad genérica para visualizar el árbol AVL en consola
 */
public class TreeVisualizer {
    
    /**
     * Imprime el árbol en formato jerárquico
     */
    public static <T extends Comparable<T>> void printHierarchicalTree(Node<T> root) {
        if (root == null) {
            System.out.println("El árbol está vacío");
            return;
        }
        
        System.out.println("\n=== ESTRUCTURA JERÁRQUICA ===");
        printHierarchical(root, "", true);
    }
    
    private static <T extends Comparable<T>> void printHierarchical(Node<T> node, String prefix, boolean isLast) {
        if (node == null) {
            return;
        }
        
        System.out.println(prefix + (isLast ? "└── " : "├── ") + 
                          "[" + node.toDisplayString() + "]");
        
        List<Node<T>> children = new ArrayList<>();
        if (node.getLeft() != null) children.add(node.getLeft());
        if (node.getRight() != null) children.add(node.getRight());
        
        for (int i = 0; i < children.size(); i++) {
            boolean isLastChild = (i == children.size() - 1);
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            
            if (children.get(i) == node.getLeft()) {
                System.out.println(newPrefix + (isLastChild && node.getRight() == null ? "└── " : "├── ") + "L:");
                printHierarchical(node.getLeft(), newPrefix + (isLastChild && node.getRight() == null ? "    " : "│   "), 
                                node.getRight() == null);
            } else {
                System.out.println(newPrefix + "└── R:");
                printHierarchical(node.getRight(), newPrefix + "    ", true);
            }
        }
    }
    
    /**
     * Imprime el árbol de forma simple (recorrido inorder)
     */
    public static <T extends Comparable<T>> void printSimpleTree(Node<T> root) {
        if (root == null) {
            System.out.println("El árbol está vacío");
            return;
        }
        
        System.out.println("\n=== RECORRIDO INORDER ===");
        printInorder(root);
        System.out.println();
    }
    
    private static <T extends Comparable<T>> void printInorder(Node<T> node) {
        if (node != null) {
            printInorder(node.getLeft());
            System.out.print("[" + node.toDisplayString() + "] ");
            printInorder(node.getRight());
        }
    }
    
    /**
     * Imprime información detallada del árbol
     */
    public static <T extends Comparable<T>> void printTreeInfo(Node<T> root) {
        if (root == null) {
            System.out.println("El árbol está vacío");
            return;
        }
        
        System.out.println("\n=== INFORMACIÓN DEL ÁRBOL ===");
        System.out.println("Altura del árbol: " + getHeight(root));
        System.out.println("Número de nodos: " + countNodes(root));
        System.out.println("Nodo raíz: [" + root.toDisplayString() + "]");
        System.out.println("Está balanceado: " + isBalanced(root));
        
        // Mostrar estadísticas por nivel
        System.out.println("\nNodos por nivel:");
        printLevelStats(root);
    }
    
    private static <T extends Comparable<T>> int getHeight(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return node.getHeight();
    }
    
    private static <T extends Comparable<T>> int countNodes(Node<T> node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.getLeft()) + countNodes(node.getRight());
    }
    
    private static <T extends Comparable<T>> boolean isBalanced(Node<T> node) {
        if (node == null) {
            return true;
        }
        
        int balance = getHeight(node.getLeft()) - getHeight(node.getRight());
        
        return Math.abs(balance) <= 1 && 
               isBalanced(node.getLeft()) && 
               isBalanced(node.getRight());
    }
    
    private static <T extends Comparable<T>> void printLevelStats(Node<T> root) {
        int height = getHeight(root);
        for (int i = 1; i <= height; i++) {
            int nodesAtLevel = countNodesAtLevel(root, i);
            System.out.println("Nivel " + i + ": " + nodesAtLevel + " nodos");
        }
    }
    
    private static <T extends Comparable<T>> int countNodesAtLevel(Node<T> node, int level) {
        if (node == null) {
            return 0;
        }
        if (level == 1) {
            return 1;
        }
        return countNodesAtLevel(node.getLeft(), level - 1) + 
               countNodesAtLevel(node.getRight(), level - 1);
    }
    
    /**
     * Muestra un menú de opciones de visualización
     */
    public static <T extends Comparable<T>> void showVisualizationMenu(Node<T> root) {
        System.out.println("\n=== OPCIONES DE VISUALIZACIÓN ===");
        System.out.println("1. Estructura jerárquica");
        System.out.println("2. Recorrido inorder");
        System.out.println("3. Información detallada del árbol");
        System.out.println("4. Todas las visualizaciones");
        System.out.println("0. Regresar al menú principal");
    }
    
    public static <T extends Comparable<T>> void executeVisualization(Node<T> root, int option) {
        switch (option) {
            case 1:
                printHierarchicalTree(root);
                break;
            case 2:
                printSimpleTree(root);
                break;
            case 3:
                printTreeInfo(root);
                break;
            case 4:
                printHierarchicalTree(root);
                printSimpleTree(root);
                printTreeInfo(root);
                break;
            default:
                System.out.println("Opción no válida");
        }
    }
}
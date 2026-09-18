package com.avltree.service;

import com.avltree.model.Node;
import com.avltree.util.TreeVisualizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del árbol AVL: comportamiento de conjunto ordenado, rotaciones y
 * conservación de las invariantes AVL.
 */
class AVLTreeTest {

    private AVLTree<Integer> tree;

    @BeforeEach
    void setUp() {
        tree = new AVLTree<>();
    }

    /**
     * Comprueba las tres invariantes del árbol: orden BST, factor de balance
     * en [-1, 1] con alturas almacenadas coherentes, y contador de tamaño
     * igual al número real de nodos.
     */
    private <T extends Comparable<T>> void assertEsAvlValido(AVLTree<T> arbol) {
        assertTrue(TreeVisualizer.isBalanced(arbol.getRoot()),
                "El árbol dejó de cumplir la propiedad AVL (balance o alturas)");
        assertEquals(TreeVisualizer.countNodes(arbol.getRoot()), arbol.size(),
                "size() no coincide con el número real de nodos");

        List<T> enOrden = arbol.toSortedList();
        for (int i = 1; i < enOrden.size(); i++) {
            assertTrue(enOrden.get(i - 1).compareTo(enOrden.get(i)) < 0,
                    "El recorrido inorder no está estrictamente ordenado");
        }
    }

    @Nested
    @DisplayName("Árbol vacío")
    class ArbolVacio {

        @Test
        void empiezaVacio() {
            assertTrue(tree.isEmpty());
            assertEquals(0, tree.size());
            assertEquals(0, tree.height());
            assertNull(tree.getRoot());
            assertTrue(tree.toSortedList().isEmpty());
        }

        @Test
        void buscarEnArbolVacioDevuelveNull() {
            assertNull(tree.search(42));
            assertFalse(tree.contains(42));
        }

        @Test
        void eliminarEnArbolVacioDevuelveFalse() {
            assertFalse(tree.delete(42));
            assertTrue(tree.isEmpty());
        }
    }

    @Nested
    @DisplayName("Inserción")
    class Insercion {

        @Test
        void insertarUnElementoLoHaceBuscable() {
            assertTrue(tree.insert(10));

            assertFalse(tree.isEmpty());
            assertEquals(1, tree.size());
            assertEquals(1, tree.height());
            assertTrue(tree.contains(10));
            assertEquals(10, tree.search(10).getData());
        }

        @Test
        void insertarClaveDuplicadaActualizaSinCrearNodo() {
            assertTrue(tree.insert(10));
            assertFalse(tree.insert(10), "La segunda inserción no debe crear un nodo nuevo");

            assertEquals(1, tree.size());
            assertEsAvlValido(tree);
        }

        @Test
        void insertarNullEsRechazado() {
            assertThrows(IllegalArgumentException.class, () -> tree.insert(null));
            assertTrue(tree.isEmpty());
        }

        @Test
        void insercionAscendenteMantieneAlturaLogaritmica() {
            for (int i = 1; i <= 1000; i++) {
                tree.insert(i);
            }

            assertEquals(1000, tree.size());
            assertEsAvlValido(tree);
            // Cota de un AVL: h <= 1.44 * log2(n+2) - 0.328 ~= 15 para n = 1000
            assertTrue(tree.height() <= 15, "Altura inesperadamente grande: " + tree.height());
        }

        @Test
        void insercionDescendenteMantieneAlturaLogaritmica() {
            for (int i = 1000; i >= 1; i--) {
                tree.insert(i);
            }

            assertEquals(1000, tree.size());
            assertEsAvlValido(tree);
            assertTrue(tree.height() <= 15, "Altura inesperadamente grande: " + tree.height());
        }
    }

    @Nested
    @DisplayName("Rotaciones")
    class Rotaciones {

        @Test
        @DisplayName("Caso izquierda-izquierda: rotación simple a la derecha")
        void casoIzquierdaIzquierda() {
            tree.insert(30);
            tree.insert(20);
            tree.insert(10);

            assertEquals(20, tree.getRoot().getData());
            assertEquals(10, tree.getRoot().getLeft().getData());
            assertEquals(30, tree.getRoot().getRight().getData());
            assertEsAvlValido(tree);
        }

        @Test
        @DisplayName("Caso derecha-derecha: rotación simple a la izquierda")
        void casoDerechaDerecha() {
            tree.insert(10);
            tree.insert(20);
            tree.insert(30);

            assertEquals(20, tree.getRoot().getData());
            assertEquals(10, tree.getRoot().getLeft().getData());
            assertEquals(30, tree.getRoot().getRight().getData());
            assertEsAvlValido(tree);
        }

        @Test
        @DisplayName("Caso izquierda-derecha: rotación doble")
        void casoIzquierdaDerecha() {
            tree.insert(30);
            tree.insert(10);
            tree.insert(20);

            assertEquals(20, tree.getRoot().getData());
            assertEquals(10, tree.getRoot().getLeft().getData());
            assertEquals(30, tree.getRoot().getRight().getData());
            assertEsAvlValido(tree);
        }

        @Test
        @DisplayName("Caso derecha-izquierda: rotación doble")
        void casoDerechaIzquierda() {
            tree.insert(10);
            tree.insert(30);
            tree.insert(20);

            assertEquals(20, tree.getRoot().getData());
            assertEquals(10, tree.getRoot().getLeft().getData());
            assertEquals(30, tree.getRoot().getRight().getData());
            assertEsAvlValido(tree);
        }

        @Test
        @DisplayName("La eliminación también rebalancea")
        void eliminarProvocaRotacion() {
            // Árbol que queda desbalanceado al quitar la hoja derecha
            Arrays.asList(50, 30, 70, 20, 40, 10).forEach(tree::insert);
            assertEsAvlValido(tree);

            assertTrue(tree.delete(70));

            assertEsAvlValido(tree);
            assertEquals(5, tree.size());
            assertFalse(tree.contains(70));
        }
    }

    @Nested
    @DisplayName("Eliminación")
    class Eliminacion {

        @BeforeEach
        void poblar() {
            Arrays.asList(50, 30, 70, 20, 40, 60, 80).forEach(tree::insert);
        }

        @Test
        void eliminarHoja() {
            assertTrue(tree.delete(20));

            assertFalse(tree.contains(20));
            assertEquals(6, tree.size());
            assertEsAvlValido(tree);
        }

        @Test
        void eliminarNodoConUnHijo() {
            tree.delete(20);
            assertTrue(tree.delete(30));

            assertFalse(tree.contains(30));
            assertTrue(tree.contains(40), "El hijo debe seguir en el árbol");
            assertEsAvlValido(tree);
        }

        @Test
        void eliminarNodoConDosHijos() {
            assertTrue(tree.delete(30));

            assertFalse(tree.contains(30));
            assertTrue(tree.contains(20));
            assertTrue(tree.contains(40));
            assertEquals(6, tree.size());
            assertEsAvlValido(tree);
        }

        @Test
        void eliminarLaRaiz() {
            assertTrue(tree.delete(50));

            assertFalse(tree.contains(50));
            assertEquals(6, tree.size());
            assertEsAvlValido(tree);
        }

        @Test
        void eliminarInexistenteDevuelveFalse() {
            assertFalse(tree.delete(999));
            assertEquals(7, tree.size());
            assertEsAvlValido(tree);
        }

        @Test
        void eliminarTodoDejaElArbolVacio() {
            for (Integer valor : new ArrayList<>(tree.toSortedList())) {
                assertTrue(tree.delete(valor));
                assertEsAvlValido(tree);
            }

            assertTrue(tree.isEmpty());
            assertEquals(0, tree.size());
            assertEquals(0, tree.height());
        }
    }

    @Nested
    @DisplayName("Actualización")
    class Actualizacion {

        @BeforeEach
        void poblar() {
            Arrays.asList(10, 20, 30).forEach(tree::insert);
        }

        @Test
        void actualizarConLaMismaClaveConservaLaEstructura() {
            assertEquals(AVLTree.UpdateResult.UPDATED, tree.update(20, 20));

            assertEquals(3, tree.size());
            assertEsAvlValido(tree);
        }

        @Test
        void actualizarConClaveNuevaMueveElNodo() {
            assertEquals(AVLTree.UpdateResult.UPDATED, tree.update(10, 40));

            assertFalse(tree.contains(10));
            assertTrue(tree.contains(40));
            assertEquals(3, tree.size());
            assertEsAvlValido(tree);
        }

        @Test
        void actualizarAUnaClaveExistenteNoDestruyeElOtroRegistro() {
            assertEquals(AVLTree.UpdateResult.KEY_CONFLICT, tree.update(10, 30));

            assertEquals(3, tree.size(), "No se debe perder ningún nodo");
            assertTrue(tree.contains(10), "El origen debe seguir intacto");
            assertTrue(tree.contains(30), "El destino debe seguir intacto");
            assertEsAvlValido(tree);
        }

        @Test
        void actualizarAlgoInexistenteNoModificaNada() {
            assertEquals(AVLTree.UpdateResult.NOT_FOUND, tree.update(999, 1000));

            assertEquals(3, tree.size());
            assertFalse(tree.contains(1000));
        }
    }

    @Nested
    @DisplayName("Propiedades sobre secuencias aleatorias")
    class Propiedades {

        @Test
        @DisplayName("El árbol se comporta igual que un TreeSet tras miles de operaciones")
        void equivaleAUnTreeSet() {
            Random random = new Random(20260918L);

            for (int ronda = 0; ronda < 50; ronda++) {
                AVLTree<Integer> avl = new AVLTree<>();
                TreeSet<Integer> referencia = new TreeSet<>();

                for (int operacion = 0; operacion < 400; operacion++) {
                    int valor = random.nextInt(120);

                    if (random.nextBoolean()) {
                        assertEquals(referencia.add(valor), avl.insert(valor),
                                "insert() debe informar si el nodo era nuevo");
                    } else {
                        assertEquals(referencia.remove(valor), avl.delete(valor),
                                "delete() debe informar si se eliminó algo");
                    }

                    assertEsAvlValido(avl);
                }

                assertEquals(new ArrayList<>(referencia), avl.toSortedList(),
                        "El contenido ordenado debe coincidir con el del TreeSet");
                assertEquals(referencia.size(), avl.size());
            }
        }
    }

    @Nested
    @DisplayName("Otras operaciones")
    class Otras {

        @Test
        void clearVaciaElArbol() {
            Arrays.asList(1, 2, 3).forEach(tree::insert);
            tree.clear();

            assertTrue(tree.isEmpty());
            assertEquals(0, tree.size());
        }

        @Test
        void setRootRecalculaElTamano() {
            AVLTree<Integer> origen = new AVLTree<>();
            Arrays.asList(10, 20, 30, 40, 50).forEach(origen::insert);

            Node<Integer> raiz = origen.getRoot();
            tree.setRoot(raiz);

            assertEquals(5, tree.size(), "setRoot debe dejar size() coherente");
            assertEsAvlValido(tree);
        }

        @Test
        void inorderTraversalDevuelveLosNodosOrdenados() {
            Arrays.asList(50, 30, 70, 20).forEach(tree::insert);

            List<Node<Integer>> nodos = tree.inorderTraversal();

            assertEquals(4, nodos.size());
            assertEquals(Arrays.asList(20, 30, 50, 70), tree.toSortedList());
        }

        @Test
        void funcionaConTiposNoNumericos() {
            AVLTree<String> palabras = new AVLTree<>();
            Arrays.asList("pera", "manzana", "uva", "banano").forEach(palabras::insert);

            assertEquals(Arrays.asList("banano", "manzana", "pera", "uva"), palabras.toSortedList());
            assertEsAvlValido(palabras);
        }
    }
}

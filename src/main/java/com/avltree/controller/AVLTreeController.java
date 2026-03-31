package com.avltree.controller;

import com.avltree.model.Node;
import com.avltree.model.Persona;
import com.avltree.service.AVLTree;
import com.avltree.service.TreePersistenceService;
import com.avltree.util.TreeVisualizer;
import java.util.Scanner;

/**
 * Controlador principal que maneja el menú de la aplicación para árbol AVL de Personas
 */
public class AVLTreeController {
    private AVLTree<Persona> tree;
    private TreePersistenceService<Persona> persistenceService;
    private Scanner scanner;
    
    public AVLTreeController() {
        this.tree = new AVLTree<>();
        this.scanner = new Scanner(System.in);
        this.persistenceService = new TreePersistenceService<>(tree, Persona.class);
    }
    
    /**
     * Método principal que ejecuta la aplicación
     */
    public void run() {
        showWelcomeMessage();
        
        try {
            // La conexión ya está validada, proceder a sincronizar datos
            System.out.println("🔄 Sincronizando datos existentes...");
            persistenceService.syncWithDatabase();
            System.out.println("✅ Datos sincronizados correctamente");
        } catch (Exception e) {
            System.out.println("⚠ Error al sincronizar datos: " + e.getMessage());
            System.out.println("Continuando con árbol vacío...");
        }
        
        boolean running = true;
        while (running) {
            showMainMenu();
            int option = getIntInput("Seleccione una opción: ");
            
            switch (option) {
                case 1:
                    insertPerson();
                    break;
                case 2:
                    updatePerson();
                    break;
                case 3:
                    searchPerson();
                    break;
                case 4:
                    deletePerson();
                    break;
                case 5:
                    graphTree();
                    break;
                case 6:
                    databaseMenu();
                    break;
                case 7:
                    showTreeInfo();
                    break;
                case 8:
                    showAllPersons();
                    break;
                case 0:
                    running = false;
                    shutdown();
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, intente de nuevo.");
            }
            
            if (running) {
                System.out.println("\nPresione Enter para continuar...");
                scanner.nextLine();
            }
        }
    }
    
    /**
     * Muestra el mensaje de bienvenida
     */
    private void showWelcomeMessage() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              ÁRBOL AVL DE PERSONAS CON MONGODB              ║");
        System.out.println("║          Gestión de Personas ordenadas por DPI              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Muestra el menú principal
     */
    private void showMainMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    MENÚ PRINCIPAL");
        System.out.println("=".repeat(60));
        System.out.println("1. Insertar persona");
        System.out.println("2. Actualizar persona");
        System.out.println("3. Buscar persona por DPI");
        System.out.println("4. Eliminar persona");
        System.out.println("5. Graficar árbol");
        System.out.println("6. Operaciones de base de datos");
        System.out.println("7. Información del árbol");
        System.out.println("8. Mostrar todas las personas");
        System.out.println("0. Salir");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Maneja la inserción de una persona
     */
    private void insertPerson() {
        System.out.println("\n=== INSERTAR PERSONA ===");
        
        try {
            String dpi = getStringInput("Ingrese el DPI: ");
            String nombre = getStringInput("Ingrese el nombre: ");
            String apellido = getStringInput("Ingrese el apellido: ");
            int edad = getIntInput("Ingrese la edad: ");
            
            Persona persona = new Persona(nombre, apellido, edad, dpi);
            
            if (!persona.isValid()) {
                System.out.println("Datos de persona inválidos. Verifique que:");
                System.out.println("   - Nombre y apellido no estén vacíos");
                System.out.println("   - Edad esté entre 1 y 149 años");
                System.out.println("   - DPI tenga al menos 8 caracteres");
                return;
            }
            
            tree.insert(persona);
            System.out.println("✓ Persona insertada exitosamente");
            System.out.println("✓ " + persona.getDetalle());
            
            // Guardar en base de datos si es posible
            Node<Persona> insertedNode = tree.search(persona);
            if (insertedNode != null) {
                persistenceService.saveNode(insertedNode);
            }
            
        } catch (Exception e) {
            System.out.println("Error al insertar la persona: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la actualización de una persona
     */
    private void updatePerson() {
        System.out.println("\n=== ACTUALIZAR PERSONA ===");
        
        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
            return;
        }
        
        try {
            String dpi = getStringInput("Ingrese el DPI de la persona a actualizar: ");
            
            // Crear persona temporal para búsqueda
            Persona searchPersona = new Persona("", "", 0, dpi);
            Node<Persona> node = tree.search(searchPersona);
            
            if (node == null) {
                System.out.println("❌ No se encontró una persona con el DPI " + dpi);
                return;
            }
            
            Persona currentPersona = node.getData();
            System.out.println("Persona actual: " + currentPersona.getDetalle());
            System.out.println();
            
            System.out.println("Ingrese los nuevos datos (presione Enter para mantener el valor actual):");
            
            String nuevoNombre = getStringInputOptional("Nuevo nombre [" + currentPersona.getNombre() + "]: ");
            if (nuevoNombre.isEmpty()) {
                nuevoNombre = currentPersona.getNombre();
            }
            
            String nuevoApellido = getStringInputOptional("Nuevo apellido [" + currentPersona.getApellido() + "]: ");
            if (nuevoApellido.isEmpty()) {
                nuevoApellido = currentPersona.getApellido();
            }
            
            String edadStr = getStringInputOptional("Nueva edad [" + currentPersona.getEdad() + "]: ");
            int nuevaEdad = currentPersona.getEdad();
            if (!edadStr.isEmpty()) {
                try {
                    nuevaEdad = Integer.parseInt(edadStr);
                } catch (NumberFormatException e) {
                    System.out.println("❌ Edad inválida, manteniendo valor actual");
                    nuevaEdad = currentPersona.getEdad();
                }
            }
            
            String nuevoDpi = getStringInputOptional("Nuevo DPI [" + currentPersona.getDpi() + "]: ");
            if (nuevoDpi.isEmpty()) {
                nuevoDpi = currentPersona.getDpi();
            }
            
            Persona nuevaPersona = new Persona(nuevoNombre, nuevoApellido, nuevaEdad, nuevoDpi);
            
            if (!nuevaPersona.isValid()) {
                System.out.println("❌ Los nuevos datos son inválidos");
                return;
            }
            
            boolean dpiChanged = !currentPersona.getDpi().equals(nuevaPersona.getDpi());
            boolean updated = tree.update(currentPersona, nuevaPersona);
            if (updated) {
                System.out.println("✓ Persona actualizada exitosamente");
                System.out.println("✓ " + nuevaPersona.getDetalle());

                // Eliminar registro anterior de BD si cambió el DPI
                if (dpiChanged) {
                    persistenceService.deleteNode(currentPersona);
                }

                // Guardar el registro actualizado en base de datos
                Node<Persona> updatedNode = tree.search(nuevaPersona);
                if (updatedNode != null) {
                    persistenceService.saveNode(updatedNode);
                }
            } else {
                System.out.println("❌ No se pudo actualizar la persona");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al actualizar la persona: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la búsqueda de una persona
     */
    private void searchPerson() {
        System.out.println("\n=== BUSCAR PERSONA ===");
        
        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
            return;
        }
        
        try {
            String dpi = getStringInput("Ingrese el DPI a buscar: ");
            
            // Crear persona temporal para búsqueda
            Persona searchPersona = new Persona("", "", 0, dpi);
            Node<Persona> node = tree.search(searchPersona);
            
            if (node != null) {
                Persona persona = node.getData();
                System.out.println("✓ Persona encontrada:");
                System.out.println("  " + persona.getDetalle());
                System.out.println("  Altura en árbol: " + node.getHeight());
            } else {
                System.out.println("❌ No se encontró una persona con el DPI " + dpi);
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al buscar la persona: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la eliminación de una persona
     */
    private void deletePerson() {
        System.out.println("\n=== ELIMINAR PERSONA ===");
        
        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
            return;
        }
        
        try {
            String dpi = getStringInput("Ingrese el DPI de la persona a eliminar: ");
            
            // Crear persona temporal para búsqueda
            Persona searchPersona = new Persona("", "", 0, dpi);
            Node<Persona> node = tree.search(searchPersona);
            
            if (node == null) {
                System.out.println("❌ No se encontró una persona con el DPI " + dpi);
                return;
            }
            
            Persona persona = node.getData();
            System.out.println("Persona a eliminar: " + persona.getDetalle());
            String confirm = getStringInput("¿Está seguro? (s/n): ");
            
            if (confirm.toLowerCase().startsWith("s")) {
                tree.delete(persona);
                System.out.println("✓ Persona eliminada exitosamente");
                
                // Eliminar de base de datos
                persistenceService.deleteNode(persona);
                
            } else {
                System.out.println("Operación cancelada");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al eliminar la persona: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la visualización del árbol
     */
    private void graphTree() {
        System.out.println("\n=== GRAFICAR ÁRBOL ===");
        
        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
            return;
        }
        
        boolean showingGraph = true;
        while (showingGraph) {
            TreeVisualizer.showVisualizationMenu(tree.getRoot());
            int option = getIntInput("Seleccione una opción: ");
            
            if (option == 0) {
                showingGraph = false;
            } else {
                TreeVisualizer.executeVisualization(tree.getRoot(), option);
                if (option != 4) { // Si no es "todas las visualizaciones"
                    System.out.println("\nPresione Enter para continuar...");
                    scanner.nextLine();
                }
            }
        }
    }
    
    /**
     * Muestra todas las personas ordenadas por DPI
     */
    private void showAllPersons() {
        System.out.println("\n=== TODAS LAS PERSONAS (ORDENADAS POR DPI) ===");
        
        if (tree.isEmpty()) {
            System.out.println("⚠ No hay personas registradas");
            return;
        }
        
        var nodes = tree.inorderTraversal();
        System.out.println("Total de personas: " + nodes.size());
        System.out.println();
        
        int count = 1;
        for (Node<Persona> node : nodes) {
            Persona persona = node.getData();
            System.out.printf("%3d. %s\n", count++, persona.getDetalle());
        }
    }
    
    /**
     * Muestra el menú de operaciones de base de datos
     */
    private void databaseMenu() {
        boolean inDatabaseMenu = true;
        
        while (inDatabaseMenu) {
            System.out.println("\n=== OPERACIONES DE BASE DE DATOS ===");
            System.out.println("1. Guardar árbol completo");
            System.out.println("2. Cargar árbol desde BD");
            System.out.println("3. Sincronizar con BD");
            System.out.println("4. Estadísticas de BD");
            System.out.println("5. Verificar integridad");
            System.out.println("6. Limpiar base de datos");
            System.out.println("0. Regresar");
            
            int option = getIntInput("Seleccione una opción: ");
            
            switch (option) {
                case 1:
                    persistenceService.saveTree();
                    break;
                case 2:
                    persistenceService.loadTree();
                    break;
                case 3:
                    persistenceService.syncWithDatabase();
                    break;
                case 4:
                    persistenceService.printDatabaseStats();
                    break;
                case 5:
                    persistenceService.verifyIntegrity();
                    break;
                case 6:
                    String confirm = getStringInput("¿Está seguro de limpiar la BD? (s/n): ");
                    if (confirm.toLowerCase().startsWith("s")) {
                        persistenceService.clearDatabase();
                    }
                    break;
                case 0:
                    inDatabaseMenu = false;
                    break;
                default:
                    System.out.println("❌ Opción no válida");
            }
            
            if (inDatabaseMenu && option != 0) {
                System.out.println("\nPresione Enter para continuar...");
                scanner.nextLine();
            }
        }
    }
    
    /**
     * Muestra información general del árbol
     */
    private void showTreeInfo() {
        System.out.println("\n=== INFORMACIÓN DEL ÁRBOL ===");
        
        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
        } else {
            TreeVisualizer.printTreeInfo(tree.getRoot());
        }
    }
    
    /**
     * Maneja el cierre de la aplicación
     */
    private void shutdown() {
        System.out.println("\n=== CERRANDO APLICACIÓN ===");
        
        try {
            // Guardar cambios antes de salir
            if (!tree.isEmpty()) {
                String save = getStringInput("¿Desea guardar los cambios en la base de datos? (s/n): ");
                if (save.toLowerCase().startsWith("s")) {
                    persistenceService.saveTree();
                }
            }
            
            System.out.println("¡Gracias por usar el sistema de Gestión de Personas con Árbol AVL!");
            
        } catch (Exception e) {
            System.out.println("Error durante el cierre: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Obtiene entrada de entero del usuario con validación
     */
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor, ingrese un número válido.");
            }
        }
    }
    
    /**
     * Obtiene entrada de cadena del usuario
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    /**
     * Obtiene entrada de cadena opcional del usuario
     */
    private String getStringInputOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
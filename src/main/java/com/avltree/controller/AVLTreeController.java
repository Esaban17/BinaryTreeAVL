package com.avltree.controller;

import com.avltree.model.Node;
import com.avltree.model.Persona;
import com.avltree.model.PersonaDocumentMapper;
import com.avltree.service.AVLTree;
import com.avltree.service.MongoDBConnection;
import com.avltree.service.TreePersistenceService;
import com.avltree.util.TreeVisualizer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Menú interactivo para gestionar el árbol AVL de personas.
 */
public class AVLTreeController {

    /** Cómo se muestra una persona dentro del árbol. */
    private static final Function<Persona, String> FORMATO_PERSONA = Persona::toShortString;

    private final AVLTree<Persona> tree;
    private final TreePersistenceService<Persona> persistenceService;
    private final Scanner scanner;

    public AVLTreeController() {
        this.tree = new AVLTree<>();
        // UTF-8 explícito para que los nombres con acentos se lean correctamente
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        this.persistenceService = new TreePersistenceService<>(tree, new PersonaDocumentMapper());
    }

    /**
     * Ejecuta el ciclo principal de la aplicación.
     */
    public void run() {
        showWelcomeMessage();
        cargarDatosIniciales();

        boolean running = true;
        while (running) {
            showMainMenu();
            int option = getIntInput("Seleccione una opción: ");

            switch (option) {
                case 1: insertPerson(); break;
                case 2: updatePerson(); break;
                case 3: searchPerson(); break;
                case 4: deletePerson(); break;
                case 5: graphTree(); break;
                case 6: databaseMenu(); break;
                case 7: showTreeInfo(); break;
                case 8: showAllPersons(); break;
                case 0:
                    running = false;
                    shutdown();
                    break;
                default:
                    System.out.println("❌ Opción no válida. Por favor, intente de nuevo.");
            }

            if (running) {
                pausar();
            }
        }
    }

    private void cargarDatosIniciales() {
        System.out.println("🔄 Sincronizando datos existentes...");
        int cargados = persistenceService.loadTree();
        if (cargados < 0) {
            System.out.println("⚠ No se pudieron cargar los datos. Continuando con árbol vacío.");
        } else if (cargados == 0) {
            System.out.println("ℹ No hay personas registradas todavía.");
        } else {
            System.out.printf("✅ %d persona(s) cargada(s) desde la base de datos%n", cargados);
        }
    }

    private void showWelcomeMessage() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              ÁRBOL AVL DE PERSONAS CON MONGODB               ║");
        System.out.println("║           Gestión de Personas ordenadas por DPI               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

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

    private void insertPerson() {
        System.out.println("\n=== INSERTAR PERSONA ===");

        String dpi = getStringInput("Ingrese el DPI: ");
        String nombre = getStringInput("Ingrese el nombre: ");
        String apellido = getStringInput("Ingrese el apellido: ");
        int edad = getIntInput("Ingrese la edad: ");

        Persona persona = new Persona(nombre, apellido, edad, dpi);
        if (!reportarSiEsInvalida(persona)) {
            return;
        }

        boolean esNueva = tree.insert(persona);
        if (esNueva) {
            System.out.println("✓ Persona insertada exitosamente");
        } else {
            System.out.println("ℹ Ya existía una persona con el DPI " + dpi + "; se actualizaron sus datos");
        }
        System.out.println("  " + persona.getDetalle());

        if (!persistenceService.saveData(persona)) {
            System.out.println("⚠ La persona está en memoria pero no se pudo guardar en la base de datos");
        }
    }

    private void updatePerson() {
        System.out.println("\n=== ACTUALIZAR PERSONA ===");

        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
            return;
        }

        String dpi = getStringInput("Ingrese el DPI de la persona a actualizar: ");
        Node<Persona> node = tree.search(Persona.claveDe(dpi));
        if (node == null) {
            System.out.println("❌ No se encontró una persona con el DPI " + dpi);
            return;
        }

        Persona actual = node.getData();
        System.out.println("Persona actual: " + actual.getDetalle());
        System.out.println("\nIngrese los nuevos datos (presione Enter para mantener el valor actual):");

        String nuevoNombre = getStringInput("Nuevo nombre [" + actual.getNombre() + "]: ", actual.getNombre());
        String nuevoApellido = getStringInput("Nuevo apellido [" + actual.getApellido() + "]: ", actual.getApellido());
        int nuevaEdad = getIntInput("Nueva edad [" + actual.getEdad() + "]: ", actual.getEdad());
        String nuevoDpi = getStringInput("Nuevo DPI [" + actual.getDpi() + "]: ", actual.getDpi());

        Persona nueva = new Persona(nuevoNombre, nuevoApellido, nuevaEdad, nuevoDpi);
        if (!reportarSiEsInvalida(nueva)) {
            return;
        }

        // Copia de los datos originales: tree.update() modifica el nodo in situ,
        // así que después de llamarlo 'actual' ya no sirve para borrar en la BD.
        Persona anterior = new Persona(actual.getNombre(), actual.getApellido(),
                actual.getEdad(), actual.getDpi());

        AVLTree.UpdateResult resultado = tree.update(anterior, nueva);
        switch (resultado) {
            case NOT_FOUND:
                System.out.println("❌ No se encontró una persona con el DPI " + dpi);
                return;
            case KEY_CONFLICT:
                System.out.println("❌ Ya existe otra persona con el DPI " + nuevoDpi + ".");
                System.out.println("   No se realizó ningún cambio para no sobrescribir ese registro.");
                return;
            case UPDATED:
            default:
                break;
        }

        System.out.println("✓ Persona actualizada exitosamente");
        System.out.println("  " + nueva.getDetalle());

        // Escribe el nuevo documento y elimina el anterior si el DPI cambió,
        // para que la versión vieja no reaparezca en la próxima carga.
        if (!persistenceService.replaceData(anterior, nueva)) {
            System.out.println("⚠ El cambio está en memoria pero no se pudo guardar en la base de datos");
        }
    }

    private void searchPerson() {
        System.out.println("\n=== BUSCAR PERSONA ===");

        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
            return;
        }

        String dpi = getStringInput("Ingrese el DPI a buscar: ");
        Node<Persona> node = tree.search(Persona.claveDe(dpi));

        if (node == null) {
            System.out.println("❌ No se encontró una persona con el DPI " + dpi);
            return;
        }

        System.out.println("✓ Persona encontrada:");
        System.out.println("  " + node.getData().getDetalle());
        System.out.println("  Altura del subárbol: " + node.getHeight());
    }

    private void deletePerson() {
        System.out.println("\n=== ELIMINAR PERSONA ===");

        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
            return;
        }

        String dpi = getStringInput("Ingrese el DPI de la persona a eliminar: ");
        Node<Persona> node = tree.search(Persona.claveDe(dpi));
        if (node == null) {
            System.out.println("❌ No se encontró una persona con el DPI " + dpi);
            return;
        }

        Persona persona = node.getData();
        System.out.println("Persona a eliminar: " + persona.getDetalle());
        if (!confirmar("¿Está seguro? (s/n): ")) {
            System.out.println("Operación cancelada");
            return;
        }

        if (!tree.delete(persona)) {
            System.out.println("❌ No se pudo eliminar la persona del árbol");
            return;
        }
        System.out.println("✓ Persona eliminada del árbol");

        if (!persistenceService.deleteData(persona)) {
            System.out.println("⚠ No se encontró el registro en la base de datos (puede que nunca se guardara)");
        }
    }

    private void graphTree() {
        System.out.println("\n=== GRAFICAR ÁRBOL ===");

        if (tree.isEmpty()) {
            System.out.println("⚠ El árbol está vacío");
            return;
        }

        while (true) {
            TreeVisualizer.showVisualizationMenu();
            int option = getIntInput("Seleccione una opción: ");
            if (option == 0) {
                return;
            }
            TreeVisualizer.executeVisualization(tree.getRoot(), option, FORMATO_PERSONA);
            pausar();
        }
    }

    private void showAllPersons() {
        System.out.println("\n=== TODAS LAS PERSONAS (ORDENADAS POR DPI) ===");

        if (tree.isEmpty()) {
            System.out.println("⚠ No hay personas registradas");
            return;
        }

        List<Persona> personas = tree.toSortedList();
        System.out.println("Total de personas: " + personas.size() + "\n");

        int count = 1;
        for (Persona persona : personas) {
            System.out.printf("%3d. %s%n", count++, persona.getDetalle());
        }
    }

    private void databaseMenu() {
        while (true) {
            System.out.println("\n=== OPERACIONES DE BASE DE DATOS ===");
            System.out.println("1. Guardar árbol completo");
            System.out.println("2. Cargar árbol desde BD");
            System.out.println("3. Estadísticas de BD");
            System.out.println("4. Verificar integridad");
            System.out.println("5. Limpiar base de datos");
            System.out.println("0. Regresar");

            int option = getIntInput("Seleccione una opción: ");

            switch (option) {
                case 1: guardarArbol(); break;
                case 2: cargarArbol(); break;
                case 3: mostrarEstadisticas(); break;
                case 4: verificarIntegridad(); break;
                case 5: limpiarBaseDeDatos(); break;
                case 0: return;
                default:
                    System.out.println("❌ Opción no válida");
            }

            pausar();
        }
    }

    private void guardarArbol() {
        int guardados = persistenceService.saveTree();
        if (guardados < 0) {
            System.out.println("❌ No se pudo guardar el árbol. Revise el log para más detalles.");
        } else {
            System.out.printf("✓ Árbol guardado: %d nodo(s) en la base de datos%n", guardados);
        }
    }

    private void cargarArbol() {
        int cargados = persistenceService.loadTree();
        if (cargados < 0) {
            System.out.println("❌ No se pudo cargar el árbol. Revise el log para más detalles.");
        } else {
            System.out.printf("✓ Árbol cargado: %d persona(s)%n", cargados);
        }
    }

    private void mostrarEstadisticas() {
        System.out.println("\n=== ESTADÍSTICAS ===");
        System.out.println("Personas en memoria: " + tree.size());
        System.out.println("Altura del árbol: " + tree.height());

        long documentos = persistenceService.countDocuments();
        if (documentos < 0) {
            System.out.println("Documentos en MongoDB: no disponible");
        } else {
            System.out.println("Documentos en MongoDB: " + documentos);
        }

        String baseDeDatos = MongoDBConnection.getInstance().getDatabaseName();
        if (baseDeDatos != null) {
            System.out.println("Base de datos: " + baseDeDatos);
        }
    }

    private void verificarIntegridad() {
        TreePersistenceService.IntegrityReport reporte = persistenceService.verifyIntegrity();

        System.out.println("\n=== VERIFICACIÓN DE INTEGRIDAD ===");
        if (reporte.getError() != null) {
            System.out.println("❌ No se pudo verificar: " + reporte.getError());
            return;
        }

        System.out.println("Nodos en el árbol: " + reporte.getNodosEnArbol());
        System.out.println("Documentos en la base de datos: " + reporte.getNodosEnBaseDeDatos());

        if (reporte.isOk()) {
            System.out.println("✓ Integridad verificada correctamente");
            return;
        }

        if (!reporte.getIdentificadoresFaltantes().isEmpty()) {
            System.out.println("⚠ Registros del árbol que faltan en la base de datos:");
            for (String id : reporte.getIdentificadoresFaltantes()) {
                System.out.println("   - " + id);
            }
        }
        if (reporte.getNodosEnArbol() != reporte.getNodosEnBaseDeDatos()) {
            System.out.println("⚠ El número de registros no coincide. Guarde el árbol para sincronizarlos.");
        }
    }

    private void limpiarBaseDeDatos() {
        if (!confirmar("¿Está seguro de limpiar la BD? Esta acción no se puede deshacer (s/n): ")) {
            System.out.println("Operación cancelada");
            return;
        }
        long eliminados = persistenceService.clearDatabase();
        if (eliminados < 0) {
            System.out.println("❌ No se pudo limpiar la base de datos");
        } else {
            System.out.printf("✓ Base de datos limpiada: %d documento(s) eliminado(s)%n", eliminados);
        }
    }

    private void showTreeInfo() {
        if (tree.isEmpty()) {
            System.out.println("\n⚠ El árbol está vacío");
            return;
        }
        TreeVisualizer.printTreeInfo(tree.getRoot(), FORMATO_PERSONA);
    }

    private void shutdown() {
        System.out.println("\n=== CERRANDO APLICACIÓN ===");

        try {
            if (!tree.isEmpty() && confirmar("¿Desea guardar los cambios en la base de datos? (s/n): ")) {
                guardarArbol();
            }
            System.out.println("¡Gracias por usar el sistema de Gestión de Personas con Árbol AVL!");
        } finally {
            scanner.close();
            MongoDBConnection.getInstance().close();
        }
    }

    /**
     * Imprime los errores de validación de la persona.
     *
     * @return true si la persona es válida
     */
    private boolean reportarSiEsInvalida(Persona persona) {
        List<String> errores = persona.validationErrors();
        if (errores.isEmpty()) {
            return true;
        }
        System.out.println("❌ Datos inválidos:");
        for (String error : errores) {
            System.out.println("   - " + error);
        }
        return false;
    }

    private void pausar() {
        System.out.println("\nPresione Enter para continuar...");
        scanner.nextLine();
    }

    private boolean confirmar(String prompt) {
        return getStringInput(prompt).toLowerCase().startsWith("s");
    }

    /**
     * Lee un entero, repitiendo la pregunta hasta obtener uno válido.
     */
    private int getIntInput(String prompt) {
        while (true) {
            String input = getStringInput(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor, ingrese un número válido.");
            }
        }
    }

    /**
     * Lee un entero; si la entrada está vacía devuelve el valor por defecto.
     */
    private int getIntInput(String prompt, int defaultValue) {
        String input = getStringInput(prompt);
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Valor no numérico, se mantiene " + defaultValue);
            return defaultValue;
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Lee una cadena; si la entrada está vacía devuelve el valor por defecto.
     */
    private String getStringInput(String prompt, String defaultValue) {
        String input = getStringInput(prompt);
        return input.isEmpty() ? defaultValue : input;
    }
}

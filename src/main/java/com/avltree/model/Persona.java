package com.avltree.model;

import org.bson.Document;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Persona identificada por su DPI (Documento Personal de Identificación).
 *
 * <p>El DPI es la clave natural: es lo que ordena el árbol AVL, lo que define la
 * igualdad y lo que se usa como identificador en la base de datos.
 */
public class Persona implements Comparable<Persona> {

    /** Longitud mínima aceptada para un DPI. */
    public static final int DPI_LONGITUD_MINIMA = 8;

    private static final Comparator<Persona> POR_DPI =
            Comparator.comparing(Persona::getDpi, Comparator.nullsFirst(Comparator.naturalOrder()));

    private String nombre;
    private String apellido;
    private int edad;
    private String dpi;

    /** Constructor vacío requerido por la deserialización. */
    public Persona() {
    }

    public Persona(String nombre, String apellido, int edad, String dpi) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.dpi = dpi;
    }

    /**
     * Crea una persona que solo sirve para buscar por DPI dentro del árbol.
     */
    public static Persona claveDe(String dpi) {
        Persona persona = new Persona();
        persona.setDpi(dpi);
        return persona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    /**
     * Ordena por DPI. Tolera DPI null para no romper el árbol ante datos
     * incompletos que provengan de la base de datos.
     */
    @Override
    public int compareTo(Persona other) {
        if (other == null) {
            return 1;
        }
        return POR_DPI.compare(this, other);
    }

    public Document toDocument() {
        return new Document()
                .append("nombre", nombre)
                .append("apellido", apellido)
                .append("edad", edad)
                .append("dpi", dpi);
    }

    /**
     * Crea una persona desde un documento de MongoDB.
     *
     * @throws IllegalArgumentException si el documento no trae un DPI utilizable,
     *         ya que sin clave la persona no puede ubicarse en el árbol
     */
    public static Persona fromDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        String dpi = doc.getString("dpi");
        if (dpi == null || dpi.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento no contiene un DPI válido: " + doc.toJson());
        }

        Persona persona = new Persona();
        persona.setNombre(doc.getString("nombre"));
        persona.setApellido(doc.getString("apellido"));
        persona.setEdad(doc.getInteger("edad", 0));
        persona.setDpi(dpi.trim());
        return persona;
    }

    public String getNombreCompleto() {
        return ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
    }

    /**
     * @return true si todos los campos cumplen las reglas de validación
     */
    public boolean isValid() {
        return validationErrors().isEmpty();
    }

    /**
     * Describe qué reglas de validación incumple la persona.
     *
     * @return lista vacía si los datos son válidos
     */
    public List<String> validationErrors() {
        List<String> errores = new ArrayList<>();
        if (nombre == null || nombre.trim().isEmpty()) {
            errores.add("El nombre no puede estar vacío");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            errores.add("El apellido no puede estar vacío");
        }
        if (edad <= 0 || edad >= 150) {
            errores.add("La edad debe estar entre 1 y 149 años (recibido: " + edad + ")");
        }
        if (dpi == null || dpi.trim().isEmpty()) {
            errores.add("El DPI no puede estar vacío");
        } else if (dpi.trim().length() < DPI_LONGITUD_MINIMA) {
            errores.add("El DPI debe tener al menos " + DPI_LONGITUD_MINIMA + " caracteres");
        }
        return errores;
    }

    public String getDetalle() {
        return String.format("DPI: %s | %s %s | Edad: %d", dpi, nombre, apellido, edad);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Persona persona = (Persona) obj;
        return Objects.equals(dpi, persona.dpi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dpi);
    }

    @Override
    public String toString() {
        return String.format("Persona{dpi='%s', nombre='%s', apellido='%s', edad=%d}",
                dpi, nombre, apellido, edad);
    }

    /** Representación compacta para mostrar en el árbol. */
    public String toShortString() {
        return String.format("%s (%s)", getNombreCompleto(), dpi);
    }
}

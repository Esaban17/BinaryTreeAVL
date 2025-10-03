package com.avltree.model;

import org.bson.Document;
import java.util.Objects;

/**
 * Clase Persona que implementa Comparable para ser usada en el árbol AVL
 * La comparación se realiza por el campo DPI (Documento Personal de Identificación)
 */
public class Persona implements Comparable<Persona> {
    private String nombre;
    private String apellido;
    private int edad;
    private String dpi; // Documento Personal de Identificación - clave de comparación
    
    // Constructor vacío para MongoDB
    public Persona() {}
    
    public Persona(String nombre, String apellido, int edad, String dpi) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.dpi = dpi;
    }
    
    // Getters y Setters
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
     * Implementación de Comparable - comparación por DPI
     * @param other la persona a comparar
     * @return valor negativo si es menor, 0 si es igual, positivo si es mayor
     */
    @Override
    public int compareTo(Persona other) {
        if (other == null) {
            return 1;
        }
        return this.dpi.compareTo(other.dpi);
    }
    
    /**
     * Convierte la persona a un documento de MongoDB
     */
    public Document toDocument() {
        return new Document()
                .append("nombre", nombre)
                .append("apellido", apellido)
                .append("edad", edad)
                .append("dpi", dpi);
    }
    
    /**
     * Crea una persona desde un documento de MongoDB
     */
    public static Persona fromDocument(Document doc) {
        if (doc == null) {
            return null;
        }
        
        Persona persona = new Persona();
        persona.setNombre(doc.getString("nombre"));
        persona.setApellido(doc.getString("apellido"));
        persona.setEdad(doc.getInteger("edad", 0));
        persona.setDpi(doc.getString("dpi"));
        
        return persona;
    }
    
    /**
     * Obtiene el nombre completo de la persona
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    /**
     * Valida que los datos de la persona sean correctos
     */
    public boolean isValid() {
        return nombre != null && !nombre.trim().isEmpty() &&
               apellido != null && !apellido.trim().isEmpty() &&
               edad > 0 && edad < 150 &&
               dpi != null && !dpi.trim().isEmpty() && 
               dpi.length() >= 8; // DPI debe tener al menos 8 caracteres
    }
    
    /**
     * Obtiene una representación detallada de la persona
     */
    public String getDetalle() {
        return String.format("DPI: %s | %s %s | Edad: %d", 
                           dpi, nombre, apellido, edad);
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
    
    /**
     * Representación compacta para mostrar en el árbol
     */
    public String toShortString() {
        return String.format("%s (%s)", getNombreCompleto(), dpi);
    }
}
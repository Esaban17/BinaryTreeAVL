package com.avltree.model;

import com.avltree.service.DocumentMapper;
import org.bson.Document;

/**
 * Mapeo entre {@link Persona} y documentos de MongoDB.
 *
 * <p>El identificador es el DPI, que es la clave natural de la persona y la misma
 * que ordena el árbol AVL. Así, dos personas distintas nunca comparten {@code _id}.
 */
public class PersonaDocumentMapper implements DocumentMapper<Persona> {

    @Override
    public String id(Persona persona) {
        if (persona == null || persona.getDpi() == null || persona.getDpi().trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede identificar una persona sin DPI");
        }
        return "persona_" + persona.getDpi().trim();
    }

    @Override
    public Document toDocument(Persona persona) {
        return persona.toDocument();
    }

    @Override
    public Persona fromDocument(Document document) {
        return Persona.fromDocument(document);
    }
}

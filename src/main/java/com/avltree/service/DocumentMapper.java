package com.avltree.service;

import org.bson.Document;

/**
 * Traduce entre objetos de dominio y documentos de MongoDB.
 *
 * <p>Sustituye a la serialización por reflexión que se usaba antes: allí un tipo
 * sin los métodos esperados fallaba en silencio y el nodo se perdía. Aquí el
 * contrato es explícito y el compilador lo verifica.
 *
 * @param <T> tipo de dato almacenado en el árbol
 */
public interface DocumentMapper<T extends Comparable<T>> {

    /**
     * Identificador estable y único del dato, usado como {@code _id} en MongoDB.
     *
     * <p>Debe derivarse de la clave natural del objeto (por ejemplo el DPI de una
     * persona). Nunca debe basarse en {@code hashCode()}: dos objetos distintos
     * pueden compartir hash y uno sobrescribiría al otro.
     *
     * @return identificador no nulo y no vacío
     */
    String id(T data);

    /**
     * Convierte el dato a un documento de MongoDB, sin incluir el {@code _id}.
     */
    Document toDocument(T data);

    /**
     * Reconstruye el dato desde un documento de MongoDB.
     *
     * @throws RuntimeException si el documento está incompleto o corrupto
     */
    T fromDocument(Document document);
}

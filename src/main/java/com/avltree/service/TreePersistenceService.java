package com.avltree.service;

import com.avltree.model.Node;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.nin;

/**
 * Persistencia del árbol AVL en MongoDB.
 *
 * <p>Los identificadores de los documentos provienen del {@link DocumentMapper},
 * es decir de la clave natural del dato. Los métodos devuelven el resultado de
 * cada operación para que la capa de presentación pueda informar al usuario; los
 * detalles técnicos van al log, no a la consola.
 *
 * @param <T> tipo de dato que debe implementar Comparable
 */
public class TreePersistenceService<T extends Comparable<T>> {

    private static final Logger log = LoggerFactory.getLogger(TreePersistenceService.class);

    private final MongoDBConnection mongoConnection;
    private final AVLTree<T> tree;
    private final DocumentMapper<T> mapper;

    public TreePersistenceService(AVLTree<T> tree, DocumentMapper<T> mapper) {
        this.tree = tree;
        this.mapper = mapper;
        this.mongoConnection = MongoDBConnection.getInstance();
    }

    private MongoCollection<Document> collection() {
        return mongoConnection.getCollection();
    }

    /**
     * Guarda el árbol completo en MongoDB.
     *
     * <p>Primero escribe (upsert) todos los nodos y sólo después borra los
     * documentos que ya no están en el árbol. De esa forma un fallo a mitad de
     * camino nunca deja la colección vacía, a diferencia de borrar antes de
     * escribir.
     *
     * @return número de nodos guardados, o -1 si la operación falló
     */
    public int saveTree() {
        try {
            List<Document> documentos = new ArrayList<>();
            Set<Object> identificadores = new HashSet<>();
            collectNodes(tree.getRoot(), documentos, identificadores);

            if (documentos.isEmpty()) {
                long borrados = collection().deleteMany(new Document()).getDeletedCount();
                log.info("Árbol vacío: se eliminaron {} documentos remanentes", borrados);
                return 0;
            }

            List<WriteModel<Document>> operaciones = new ArrayList<>(documentos.size());
            ReplaceOptions upsert = new ReplaceOptions().upsert(true);
            for (Document doc : documentos) {
                operaciones.add(new ReplaceOneModel<>(eq("_id", doc.get("_id")), doc, upsert));
            }

            collection().bulkWrite(operaciones);

            // Ya están escritos todos los nodos vigentes: ahora sí es seguro
            // eliminar los documentos que quedaron huérfanos.
            long huerfanos = collection().deleteMany(nin("_id", identificadores)).getDeletedCount();
            log.info("Árbol guardado: {} nodos, {} documentos obsoletos eliminados",
                    documentos.size(), huerfanos);

            return documentos.size();

        } catch (Exception e) {
            log.error("Error al guardar el árbol en MongoDB", e);
            return -1;
        }
    }

    /**
     * Carga el árbol desde MongoDB, reemplazando el contenido en memoria.
     *
     * @return número de nodos cargados, o -1 si la operación falló
     */
    public int loadTree() {
        try {
            tree.clear();

            int cargados = 0;
            int descartados = 0;

            for (Document doc : collection().find()) {
                try {
                    T data = mapper.fromDocument((Document) doc.get("data"));
                    if (data == null) {
                        descartados++;
                        continue;
                    }
                    tree.insert(data);
                    cargados++;
                } catch (RuntimeException e) {
                    descartados++;
                    log.warn("Documento ignorado por no poder deserializarse (_id={}): {}",
                            doc.get("_id"), e.getMessage());
                }
            }

            if (descartados > 0) {
                log.warn("Se ignoraron {} documentos corruptos o incompletos", descartados);
            }
            log.info("Árbol cargado desde MongoDB: {} nodos", cargados);
            return cargados;

        } catch (Exception e) {
            log.error("Error al cargar el árbol desde MongoDB", e);
            return -1;
        }
    }

    /**
     * Guarda o actualiza un único nodo.
     */
    public boolean saveNode(Node<T> node) {
        if (node == null) {
            return false;
        }
        return saveData(node.getData());
    }

    /**
     * Guarda o actualiza un único dato.
     */
    public boolean saveData(T data) {
        try {
            String identificador = mapper.id(data);
            Document doc = documentoDe(data, identificador);
            collection().replaceOne(eq("_id", identificador), doc, new ReplaceOptions().upsert(true));
            log.info("Nodo guardado en MongoDB: {}", identificador);
            return true;
        } catch (Exception e) {
            log.error("Error al guardar el nodo en MongoDB", e);
            return false;
        }
    }

    /**
     * Reemplaza un dato por otro cuya clave cambió.
     *
     * <p>Escribe el nuevo documento y elimina el anterior. Sin esto, cambiar la
     * clave de un registro dejaba el documento viejo huérfano en la base y la
     * versión anterior reaparecía en la siguiente carga.
     */
    public boolean replaceData(T oldData, T newData) {
        try {
            String idNuevo = mapper.id(newData);
            String idViejo = mapper.id(oldData);

            collection().replaceOne(eq("_id", idNuevo), documentoDe(newData, idNuevo),
                    new ReplaceOptions().upsert(true));

            if (!idViejo.equals(idNuevo)) {
                long borrados = collection().deleteOne(eq("_id", idViejo)).getDeletedCount();
                log.info("Nodo reemplazado en MongoDB: {} -> {} (documentos antiguos eliminados: {})",
                        idViejo, idNuevo, borrados);
            } else {
                log.info("Nodo actualizado en MongoDB: {}", idNuevo);
            }
            return true;

        } catch (Exception e) {
            log.error("Error al reemplazar el nodo en MongoDB", e);
            return false;
        }
    }

    /**
     * Elimina un dato de MongoDB.
     *
     * @return true si se eliminó algún documento
     */
    public boolean deleteData(T data) {
        try {
            String identificador = mapper.id(data);
            long eliminados = collection().deleteOne(eq("_id", identificador)).getDeletedCount();
            if (eliminados > 0) {
                log.info("Nodo eliminado de MongoDB: {}", identificador);
                return true;
            }
            log.warn("No se encontró el nodo en MongoDB: {}", identificador);
            return false;
        } catch (Exception e) {
            log.error("Error al eliminar el nodo de MongoDB", e);
            return false;
        }
    }

    /**
     * Busca un dato directamente en MongoDB, sin pasar por el árbol.
     */
    public T findInDatabase(T data) {
        try {
            Document doc = collection().find(eq("_id", mapper.id(data))).first();
            if (doc == null) {
                return null;
            }
            return mapper.fromDocument((Document) doc.get("data"));
        } catch (Exception e) {
            log.error("Error al buscar el nodo en MongoDB", e);
            return null;
        }
    }

    /**
     * Número de documentos almacenados, o -1 si no se pudo consultar.
     */
    public long countDocuments() {
        try {
            return collection().countDocuments();
        } catch (Exception e) {
            log.error("Error al contar los documentos en MongoDB", e);
            return -1;
        }
    }

    /**
     * Elimina todos los datos de la colección.
     *
     * @return número de documentos eliminados, o -1 si la operación falló
     */
    public long clearDatabase() {
        try {
            long eliminados = collection().deleteMany(new Document()).getDeletedCount();
            log.info("Base de datos limpiada: {} documentos eliminados", eliminados);
            return eliminados;
        } catch (Exception e) {
            log.error("Error al limpiar la base de datos", e);
            return -1;
        }
    }

    /**
     * Compara el árbol en memoria contra la base de datos.
     *
     * @return el detalle de la comparación
     */
    public IntegrityReport verifyIntegrity() {
        try {
            List<Node<T>> nodos = tree.inorderTraversal();
            long enBaseDeDatos = collection().countDocuments();

            List<String> faltantes = new ArrayList<>();
            for (Node<T> node : nodos) {
                String identificador = mapper.id(node.getData());
                if (collection().find(eq("_id", identificador)).first() == null) {
                    faltantes.add(identificador);
                }
            }

            return new IntegrityReport(nodos.size(), enBaseDeDatos, faltantes, null);

        } catch (Exception e) {
            log.error("Error durante la verificación de integridad", e);
            return new IntegrityReport(tree.size(), -1, new ArrayList<>(), e.getMessage());
        }
    }

    private Document documentoDe(T data, String identificador) {
        return new Document("_id", identificador)
                .append("data", mapper.toDocument(data));
    }

    /**
     * Recolecta los nodos del árbol como documentos listos para escribir.
     *
     * @throws IllegalStateException si dos nodos generan el mismo identificador
     */
    private void collectNodes(Node<T> node, List<Document> documentos, Set<Object> identificadores) {
        if (node == null) {
            return;
        }
        String identificador = mapper.id(node.getData());
        if (!identificadores.add(identificador)) {
            throw new IllegalStateException(
                    "Dos nodos distintos generan el mismo identificador '" + identificador
                            + "'. Guardar sobrescribiría uno de ellos.");
        }
        documentos.add(documentoDe(node.getData(), identificador));
        collectNodes(node.getLeft(), documentos, identificadores);
        collectNodes(node.getRight(), documentos, identificadores);
    }

    /** Resultado de {@link #verifyIntegrity()}. */
    public static class IntegrityReport {
        private final int nodosEnArbol;
        private final long nodosEnBaseDeDatos;
        private final List<String> identificadoresFaltantes;
        private final String error;

        IntegrityReport(int nodosEnArbol, long nodosEnBaseDeDatos,
                        List<String> identificadoresFaltantes, String error) {
            this.nodosEnArbol = nodosEnArbol;
            this.nodosEnBaseDeDatos = nodosEnBaseDeDatos;
            this.identificadoresFaltantes = identificadoresFaltantes;
            this.error = error;
        }

        public int getNodosEnArbol() {
            return nodosEnArbol;
        }

        public long getNodosEnBaseDeDatos() {
            return nodosEnBaseDeDatos;
        }

        public List<String> getIdentificadoresFaltantes() {
            return identificadoresFaltantes;
        }

        public String getError() {
            return error;
        }

        public boolean isOk() {
            return error == null
                    && identificadoresFaltantes.isEmpty()
                    && nodosEnArbol == nodosEnBaseDeDatos;
        }
    }
}

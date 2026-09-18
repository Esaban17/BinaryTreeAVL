# 🏗️ Arquitectura del Sistema AVL Genérico

## Visión General

Este documento describe la arquitectura técnica del **Sistema AVL Genérico con MongoDB**, incluyendo el diseño de clases, patrones utilizados, y la estructura de componentes.

## 📐 Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                     CAPA DE PRESENTACIÓN                    │
├─────────────────────────────────────────────────────────────┤
│  Main.java                 │  AVLTreeController.java        │
│  - Punto de entrada        │  - Interfaz de usuario         │
│  - Configuración inicial   │  - Manejo del menú             │
│  - Validación de conexión  │  - Validación de entrada       │
└─────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAPA DE NEGOCIO                        │
├─────────────────────────────────────────────────────────────┤
│  AVLTree<T>.java           │  TreePersistenceService<T>     │
│  - Operaciones del árbol   │  - Serialización genérica      │
│  - Algoritmos AVL          │  - Persistencia en MongoDB     │
│  - Balanceo automático     │  - Conversión Object↔Document  │
└─────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAPA DE MODELO                         │
├─────────────────────────────────────────────────────────────┤
│  Node<T>.java              │  Persona.java                  │
│  - Nodo genérico           │  - Implementación específica   │
│  - Estructura de datos     │  - Comparable por DPI          │
│  - Altura y balance        │  - Ejemplo de uso              │
└─────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE UTILIDADES                       │
├─────────────────────────────────────────────────────────────┤
│  TreeVisualizer.java       │  EnvLoader.java                │
│  - Visualización del árbol │  - Carga de variables .env     │
│  - Formatos múltiples      │  - Configuración segura        │
└─────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                 CAPA DE PERSISTENCIA                        │
├─────────────────────────────────────────────────────────────┤
│  MongoDBConnection.java                                     │
│  - Singleton de conexión                                   │
│  - Gestión de reintentos                                   │
│  - Configuración de cliente MongoDB                        │
└─────────────────────────────────────────────────────────────┘
```

## 🧩 Componentes Principales

### 1. **Main.java** - Punto de Entrada
```java
📋 Responsabilidades:
├── Configuración de la salida de consola en UTF-8
├── Validación de conexión MongoDB con reintentos
├── Inicialización del controlador
└── Cierre ordenado de recursos (shutdown hook)

🔧 Notas:
├── El nivel de log se define en logback.xml, no en código
└── Los reintentos esperan RETRY_INTERVAL entre intentos
```

### 2. **AVLTreeController.java** - Controlador Principal
```java
📋 Responsabilidades:
├── Interfaz de usuario (menú interactivo)
├── Validación de datos de entrada
├── Coordinación entre servicios
└── Manejo de excepciones específicas

🔧 Patrones:
├── Controller (MVC)
├── Command Pattern (opciones del menú)
└── Facade (simplifica operaciones complejas)
```

### 3. **AVLTree<T>.java** - Árbol AVL Genérico
```java
📋 Responsabilidades:
├── Operaciones CRUD del árbol
├── Algoritmos de balanceo AVL
├── Rotaciones (simple y doble)
└── Recorrido inorder

🔧 Patrones:
├── Generic Programming (T extends Comparable<T>)
└── Template Method (operaciones recursivas)

📤 Valores de retorno:
├── insert() indica si creó un nodo o actualizó uno existente
├── delete() indica si realmente eliminó algo
└── update() devuelve UPDATED, NOT_FOUND o KEY_CONFLICT

⚖️ Algoritmos de Balanceo:
├── Factor de balance: height(left) - height(right)
├── Rotación simple derecha (LL case)
├── Rotación simple izquierda (RR case)
├── Rotación doble izq-der (LR case)
└── Rotación doble der-izq (RL case)
```

### 4. **Node<T>.java** - Nodo Genérico
```java
📋 Estructura:
├── T data              // Dato genérico
├── Node<T> left        // Hijo izquierdo
├── Node<T> right       // Hijo derecho
└── int height          // Altura para AVL

🔧 Características:
├── Estructura de datos pura: no conoce MongoDB ni BSON
├── Rechaza datos null en el constructor y en setData()
└── Mutable y sin sincronización: no es seguro entre hilos
```

### 5. **TreePersistenceService<T>.java** - Persistencia Genérica
```java
📋 Responsabilidades:
├── Operaciones CRUD en MongoDB
├── Guardado y carga del árbol completo
├── Verificación de integridad árbol ↔ base de datos
└── Delegación de la serialización al DocumentMapper

🔧 Patrones:
├── Repository Pattern
├── Data Mapper (DocumentMapper)
└── Generic Programming

🛠️ Serialización:
├── Contrato explícito: id(), toDocument(), fromDocument()
├── El _id proviene de la clave natural del dato, nunca de hashCode()
├── Un documento corrupto se registra en el log, no desaparece en silencio
└── Verificado por el compilador, no por reflexión en tiempo de ejecución

💾 Guardado seguro:
├── saveTree() escribe (upsert) todos los nodos
├── y sólo después elimina los documentos obsoletos
└── de modo que un fallo intermedio nunca vacía la colección
```

### 6. **MongoDBConnection.java** - Gestión de Conexión
```java
📋 Responsabilidades:
├── Singleton de conexión MongoDB
├── Configuración de cliente
├── Reintentos automáticos
└── Pool de conexiones

🔧 Patrones:
├── Singleton (holder estático, seguro entre hilos)
├── Factory (creación de cliente)
└── Retry Pattern (reconexión)

⚙️ Configuración (todas ajustables por .env o variable de entorno):
├── CONNECTION_TIMEOUT: 10000 ms por defecto
├── SOCKET_TIMEOUT: 10000 ms por defecto
├── MAX_CONNECTION_RETRIES: 5 por defecto
├── RETRY_INTERVAL: 2000 ms por defecto
└── SSL/TLS según la URI de conexión
```

## 🎯 Patrones de Diseño Implementados

### 1. **Generic Programming**
```java
// Permite reutilización con cualquier tipo Comparable
public class AVLTree<T extends Comparable<T>> {
    private Node<T> root;
    
    public void insert(T data) {
        root = insertRec(root, data);
    }
}
```

### 2. **Singleton Pattern**
```java
// Una sola instancia de conexión MongoDB.
// El modismo del holder estático delega la exclusión mutua al cargador de
// clases: la instancia se crea la primera vez que se toca Holder, sin
// sincronización explícita ni doble comprobación.
public class MongoDBConnection {

    private static final class Holder {
        private static final MongoDBConnection INSTANCE = new MongoDBConnection();
    }

    public static MongoDBConnection getInstance() {
        return Holder.INSTANCE;
    }
}
```

### 3. **Template Method Pattern**
```java
// Estructura común para operaciones recursivas del árbol
private Node<T> insertRec(Node<T> node, T data) {
    // 1. Inserción estándar BST
    if (node == null) return new Node<>(data);
    
    // 2. Recursión
    if (data.compareTo(node.getData()) < 0)
        node.setLeft(insertRec(node.getLeft(), data));
    else if (data.compareTo(node.getData()) > 0)
        node.setRight(insertRec(node.getRight(), data));
    
    // 3. Actualizar altura y balancear (específico AVL)
    return rebalance(node);
}
```

### 4. **Data Mapper Pattern**
```java
// La traducción entre dominio y BSON vive fuera del modelo y del árbol.
// Sustituye a la serialización por reflexión, donde un tipo sin los métodos
// esperados fallaba en silencio y el nodo se perdía.
public interface DocumentMapper<T extends Comparable<T>> {
    String id(T data);                  // clave natural, nunca hashCode()
    Document toDocument(T data);
    T fromDocument(Document document);
}
```

### 5. **Repository Pattern**
```java
// Abstracción de la persistencia de datos, implementada por
// TreePersistenceService<T>. Devuelve el resultado de cada operación para
// que la capa de presentación pueda informar al usuario.
public class TreePersistenceService<T extends Comparable<T>> {
    public int saveTree();                        // nodos guardados, o -1 si falló
    public int loadTree();                        // nodos cargados, o -1 si falló
    public boolean saveData(T data);
    public boolean replaceData(T oldData, T newData);
    public boolean deleteData(T data);
    public IntegrityReport verifyIntegrity();
}
```

## 🔄 Flujo de Datos

### Inserción de Datos
```
Usuario → Controller → AVLTree → Node → PersistenceService → MongoDB
    ↑         ↓           ↓        ↓            ↓              ↓
 Feedback  Validación  Balanceo  Altura   Serialización   Storage
```

### Búsqueda de Datos
```
Usuario → Controller → AVLTree → Búsqueda O(log n) → Resultado
    ↑         ↓           ↓              ↓             ↓
 Display  Validación  Comparación    Traversal     Formato
```

### Persistencia
```
Memoria → TreePersistenceService → DocumentMapper → Document → MongoDB
   ↑              ↓                       ↓             ↓          ↓
AVLTree    Recolección de nodos    id() + toDocument()  BSON     Storage
```

## 📊 Complejidad Computacional

### Operaciones del Árbol AVL
| Operación | Tiempo | Espacio | Justificación |
|-----------|--------|---------|---------------|
| Búsqueda | O(log n) | O(1) | Árbol balanceado |
| Inserción | O(log n) | O(log n) | Recursión + rotaciones |
| Eliminación | O(log n) | O(log n) | Rebalanceo necesario |
| Traversal | O(n) | O(log n) | Visita todos los nodos |

### Operaciones de Persistencia
| Operación | Tiempo | Espacio | Justificación |
|-----------|--------|---------|---------------|
| Serializar | O(n) | O(n) | DocumentMapper en todos los nodos |
| Guardar | O(n) | O(n) | Escritura a MongoDB |
| Cargar | O(n log n) | O(n) | Inserción ordenada |

## 🧪 Estrategias de Testing

La suite vive en `src/test/java/com/avltree/` y se ejecuta con `mvn test`.
Actualmente son 76 pruebas y no requieren una instancia de MongoDB.

### 1. **Unit Testing**
```java
// Cada caso de rotación, verificado sobre la estructura resultante
@Test
void casoIzquierdaDerecha() {
    tree.insert(30);
    tree.insert(10);
    tree.insert(20);

    assertEquals(20, tree.getRoot().getData());
    assertEsAvlValido(tree);
}
```

### 2. **Property-Based Testing**
```java
// El árbol debe comportarse exactamente como un TreeSet ante cualquier
// secuencia de operaciones. Tras cada operación se comprueban las tres
// invariantes: orden inorder, factor de balance y alturas almacenadas.
@Test
void equivaleAUnTreeSet() {
    for (int ronda = 0; ronda < 50; ronda++) {
        AVLTree<Integer> avl = new AVLTree<>();
        TreeSet<Integer> referencia = new TreeSet<>();

        for (int operacion = 0; operacion < 400; operacion++) {
            int valor = random.nextInt(120);
            if (random.nextBoolean()) {
                assertEquals(referencia.add(valor), avl.insert(valor));
            } else {
                assertEquals(referencia.remove(valor), avl.delete(valor));
            }
            assertEsAvlValido(avl);
        }

        assertEquals(new ArrayList<>(referencia), avl.toSortedList());
    }
}
```

### 3. **Pruebas de regresión**
```java
// Los identificadores basados en hashCode producían colisiones reales y una
// persona sobrescribía a otra. Esta prueba fija ese caso concreto.
@Test
void colisionConocidaDeHashCodeYaNoProduceElMismoIdentificador() {
    Persona una  = new Persona("Ana",  "López", 30, "7572515731943");
    Persona otra = new Persona("Beto", "Ruiz",  40, "4086513132424");

    assertEquals(una.hashCode(), otra.hashCode());
    assertNotEquals(mapper.id(una), mapper.id(otra));
}
```

### 4. **Pendiente: Integration Testing**
La capa de persistencia todavía no se prueba contra una base de datos real.
La forma natural de cubrirla sería Testcontainers con una imagen de MongoDB,
verificando el ciclo completo de guardar, cargar y reemplazar registros.

## 🚀 Optimizaciones Implementadas

### 1. **Carga Única al Inicio**
- El árbol se carga completo de MongoDB al arrancar y se opera en memoria
- Las lecturas posteriores no tocan la red: son O(log n) sobre el árbol

### 2. **Connection Pooling**
- Reutilización de conexiones MongoDB
- Configuración optimizada para rendimiento

### 3. **Caching de Altura**
- Almacenamiento de altura en cada nodo
- Evita recálculos costosos durante balanceo

### 4. **Serialización Directa**
- Conversión explícita a BSON mediante DocumentMapper, sin reflexión
- Escrituras en bloque (bulkWrite) al guardar el árbol completo

## 🔧 Configuración y Extensibilidad

### Agregar Nuevos Tipos
```java
// 1. Implementar Comparable
public class Producto implements Comparable<Producto> {
    private String codigo;
    
    @Override
    public int compareTo(Producto otro) {
        return this.codigo.compareTo(otro.codigo);
    }
}

// 2. Implementar el DocumentMapper correspondiente
public class ProductoMapper implements DocumentMapper<Producto> {
    @Override
    public String id(Producto producto) {
        return "producto_" + producto.getCodigo();   // clave natural
    }

    @Override
    public Document toDocument(Producto producto) { /* ... */ }

    @Override
    public Producto fromDocument(Document documento) { /* ... */ }
}

// 3. Crear el árbol y su servicio de persistencia
AVLTree<Producto> inventario = new AVLTree<>();
TreePersistenceService<Producto> persistencia =
        new TreePersistenceService<>(inventario, new ProductoMapper());

// Nota: AVLTreeController está especializado en Persona. Reutilizarlo con
// otro tipo requiere generalizarlo o escribir un controlador equivalente.
```

### Personalizar Criterios de Ordenamiento
```java
// Usando Comparator personalizado
public class PersonaComparator implements Comparator<Persona> {
    @Override
    public int compare(Persona p1, Persona p2) {
        // Ordenar por apellido, luego nombre
        int result = p1.getApellido().compareTo(p2.getApellido());
        if (result == 0) {
            result = p1.getNombre().compareTo(p2.getNombre());
        }
        return result;
    }
}
```

## 📈 Métricas y Monitoreo

### Métricas del Árbol
- Altura actual vs. altura óptima
- Factor de balance por nodo
- Número de rotaciones realizadas
- Distribución de datos

### Métricas de Persistencia
- Tiempo de serialización/deserialización
- Latencia de MongoDB
- Tasa de reconexiones
- Tamaño de documentos almacenados

## 🔒 Consideraciones de Seguridad

### 1. **Protección de Credenciales**
- Variables de entorno para credenciales
- Archivo .env excluido de control de versiones
- Encriptación de conexión MongoDB (TLS/SSL)

### 2. **Validación de Entrada**
- Sanitización de datos de usuario
- Validación de tipos en tiempo de compilación
- Manejo seguro de excepciones

### 3. **Inyección de Código**
- Uso de queries parametrizadas
- Validación de entrada antes de persistencia
- Escape de caracteres especiales

---

*Esta arquitectura está diseñada para ser escalable, mantenible y extensible, siguiendo las mejores prácticas de desarrollo Java y design patterns establecidos.*
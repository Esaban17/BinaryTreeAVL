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
├── Configuración de logging (Logback)
├── Validación de conexión MongoDB
├── Inicialización del controlador
└── Manejo de errores globales

🔧 Patrones:
├── Template Method (configuración)
└── Error Handler (manejo centralizado)
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
└── Traversals (inorder, preorder, postorder)

🔧 Patrones:
├── Generic Programming (T extends Comparable<T>)
├── Strategy Pattern (diferentes traversals)
└── Template Method (operaciones recursivas)

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
├── Immutable después de creación
├── Comparable integration
└── Thread-safe design
```

### 5. **TreePersistenceService<T>.java** - Persistencia Genérica
```java
📋 Responsabilidades:
├── Serialización Object → MongoDB Document
├── Deserialización Document → Object
├── Operaciones CRUD en MongoDB
└── Manejo de tipos genéricos con Reflection

🔧 Patrones:
├── Repository Pattern
├── Data Access Object (DAO)
├── Generic Programming
└── Reflection para serialización

🛠️ Serialización:
├── Introspección de campos con Reflection
├── Conversión automática de tipos
├── Preservación de jerarquía de objetos
└── Manejo de tipos complejos (Comparable)
```

### 6. **MongoDBConnection.java** - Gestión de Conexión
```java
📋 Responsabilidades:
├── Singleton de conexión MongoDB
├── Configuración de cliente
├── Reintentos automáticos
└── Pool de conexiones

🔧 Patrones:
├── Singleton (instancia única)
├── Factory (creación de cliente)
└── Retry Pattern (reconexión)

⚙️ Configuración:
├── Connection timeout: 10s
├── Socket timeout: 10s
├── Max retries: 3
└── SSL/TLS habilitado
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
// Una sola instancia de conexión MongoDB
public class MongoDBConnection {
    private static volatile MongoDBConnection instance;
    private MongoClient mongoClient;
    
    public static MongoDBConnection getInstance() {
        if (instance == null) {
            synchronized (MongoDBConnection.class) {
                if (instance == null) {
                    instance = new MongoDBConnection();
                }
            }
        }
        return instance;
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

### 4. **Strategy Pattern**
```java
// Diferentes estrategias de visualización
public enum TraversalType {
    INORDER,
    PREORDER,
    POSTORDER,
    LEVEL_ORDER
}

public void traverse(TraversalType type) {
    switch(type) {
        case INORDER -> inorderTraversal(root);
        case PREORDER -> preorderTraversal(root);
        // ...
    }
}
```

### 5. **Repository Pattern**
```java
// Abstracción de la persistencia de datos
public interface TreeRepository<T> {
    void save(AVLTree<T> tree);
    AVLTree<T> load();
    void delete(T data);
    boolean exists(T data);
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
Memoria → TreePersistenceService → Reflection → Document → MongoDB
   ↑              ↓                     ↓          ↓         ↓
AVLTree      Serialización         Introspección  BSON    Storage
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
| Serializar | O(n) | O(n) | Reflection en todos los nodos |
| Guardar | O(n) | O(n) | Escritura a MongoDB |
| Cargar | O(n log n) | O(n) | Inserción ordenada |

## 🧪 Estrategias de Testing

### 1. **Unit Testing**
```java
// Testing de operaciones individuales
@Test
void testInsertMaintainsAVLProperty() {
    AVLTree<Integer> tree = new AVLTree<>();
    tree.insert(10);
    tree.insert(5);
    tree.insert(15);
    
    assertTrue(tree.isBalanced());
}
```

### 2. **Integration Testing**
```java
// Testing de persistencia completa
@Test
void testSaveAndLoad() {
    AVLTree<Persona> original = createTestTree();
    persistenceService.save(original);
    
    AVLTree<Persona> loaded = persistenceService.load();
    assertEquals(original.size(), loaded.size());
}
```

### 3. **Performance Testing**
```java
// Testing de rendimiento con grandes volúmenes
@Test
void testLargeVolumeInsert() {
    AVLTree<Integer> tree = new AVLTree<>();
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < 100000; i++) {
        tree.insert(i);
    }
    
    long duration = System.currentTimeMillis() - startTime;
    assertTrue(duration < 5000); // Menos de 5 segundos
}
```

## 🚀 Optimizaciones Implementadas

### 1. **Lazy Loading**
- Los datos se cargan de MongoDB solo cuando se necesitan
- Evita cargar el árbol completo en memoria al inicio

### 2. **Connection Pooling**
- Reutilización de conexiones MongoDB
- Configuración optimizada para rendimiento

### 3. **Caching de Altura**
- Almacenamiento de altura en cada nodo
- Evita recálculos costosos durante balanceo

### 4. **Serialización Optimizada**
- Uso de Reflection cacheable
- Conversión directa a BSON cuando es posible

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

// 2. Crear instancia del árbol
AVLTree<Producto> inventario = new AVLTree<>();

// 3. Usar con el mismo controller
AVLTreeController<Producto> controller = 
    new AVLTreeController<>(inventario, persistenceService);
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
# 🌳 Sistema AVL Genérico con MongoDB

Un sistema completo de gestión de datos utilizando **Árboles AVL genéricos** con persistencia en **MongoDB Atlas**. Implementado en Java con características avanzadas de visualización y manejo de tipos genéricos.

![Java](https://img.shields.io/badge/Java-17-orange)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green)
![Maven](https://img.shields.io/badge/Maven-3.9.5-blue)
![Status](https://img.shields.io/badge/Status-Production-success)

## 🚀 Características Principales

### 🔧 **Arquitectura Genérica**
- **Tipos genéricos**: `T extends Comparable<T>` para cualquier tipo de dato
- **Interfaz Comparable**: Ordenamiento automático por criterio personalizable
- **Ejemplo implementado**: Clase `Persona` ordenada por DPI

### � **Árbol AVL Avanzado**
- **Auto-balanceado**: Garantiza O(log n) en todas las operaciones
- **Operaciones completas**: Inserción, búsqueda, eliminación, actualización
- **Visualización múltiple**: Estructura jerárquica, recorrido inorder, estadísticas

### �️ **Persistencia MongoDB**
- **Serialización automática**: Reflexión para tipos genéricos
- **MongoDB Atlas**: Integración con base de datos en la nube
- **Sincronización bidireccional**: Carga y guardado automático
- **Gestión de conexión robusta**: Sistema de reintentos y validación

### 🎨 **Interfaz de Usuario**
- **Menú interactivo**: Navegación intuitiva por consola
- **Validación de datos**: Verificación automática de entrada
- **Mensajes descriptivos**: Feedback claro con emojis
- **Manejo de errores**: Recuperación elegante de fallos

## 📋 Requisitos del Sistema

- **Java**: JDK 17 o superior
- **Maven**: 3.6.0 o superior
- **MongoDB Atlas**: Cuenta con cluster configurado
- **Conexión a Internet**: Para acceso a MongoDB Atlas

## 🛠️ Instalación

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/avl-tree-mongodb.git
cd avl-tree-mongodb
```

### 2. Configurar MongoDB
Crear archivo `.env` en la raíz del proyecto:
```env
MONGODB_URI=mongodb+srv://usuario:password@cluster.mongodb.net/
DATABASE_NAME=avltree_db
COLLECTION_NAME=nodes
```

### 3. Compilar y ejecutar
```bash
# Compilar el proyecto
mvn clean compile package

# Ejecutar la aplicación
java -jar target/binary-tree-avl-1.0.0.jar
```

## 🎯 Uso del Sistema

### Ejemplo de Uso Básico

1. **Insertar personas** ordenadas automáticamente por DPI
2. **Visualizar el árbol** en formato jerárquico
3. **Buscar por DPI** con búsqueda O(log n)
4. **Persistir en MongoDB** automáticamente

```
=== SISTEMA AVL GENÉRICO ===
1. Insertar persona
2. Actualizar persona  
3. Buscar persona por DPI
4. Eliminar persona
5. Graficar árbol
6. Operaciones de base de datos
7. Información del árbol
8. Mostrar todas las personas
0. Salir
```

### Ejemplo de Visualización del Árbol

```
=== ESTRUCTURA JERÁRQUICA ===
└── [Ana Rodriguez (45678901) (h:4)]
    ├── L:
    │   ├── [maria gonzalez (23456789) (h:2)]
    │   │   ├── L:
    │   │   │   ├── [carlos perez (12345678) (h:1)]
    │   │   └── R:
    │   │       └── [Carlos lopez (34567890) (h:1)]
    └── R:
        └── [Carmen Hernandez (67890123) (h:3)]
            ├── L:
            │   ├── [Luis Martinez (56789012) (h:1)]
            └── R:
                └── [Miguel Garcia (78901234) (h:2)]
                    └── R:
                        └── [Isabel Ruiz (89012345) (h:1)]
```

## 📁 Estructura del Proyecto

```
src/main/java/com/avltree/
├── Main.java                      # Punto de entrada con validación de conexión
├── controller/
│   └── AVLTreeController.java     # Controlador principal del menú
├── model/
│   ├── Node.java                  # Nodo genérico del árbol AVL
│   └── Persona.java               # Clase de ejemplo con comparación por DPI
├── service/
│   ├── AVLTree.java               # Implementación del árbol AVL genérico
│   ├── MongoDBConnection.java     # Gestión de conexión con MongoDB
│   └── TreePersistenceService.java # Serialización y persistencia genérica
└── util/
    ├── EnvLoader.java             # Carga de variables de entorno
    └── TreeVisualizer.java        # Visualización del árbol
```

## ⚡ Características Técnicas

### Algoritmos Implementados
- **Rotaciones AVL**: Simple izquierda/derecha, doble izquierda/derecha
- **Factor de balance**: Cálculo automático y rebalanceo
- **Búsqueda binaria**: O(log n) en árbol balanceado
- **Traversal**: Inorder, preorder, postorder

### Patrones de Diseño
- **Singleton**: Conexión MongoDB
- **Template Method**: Serialización genérica
- **Strategy**: Diferentes tipos de visualización
- **Factory**: Creación de documentos MongoDB

## 🧪 Casos de Prueba

El sistema ha sido probado con:
- ✅ Inserción masiva (1000+ registros)
- ✅ Búsquedas con diferentes patrones
- ✅ Eliminaciones manteniendo balance
- ✅ Persistencia y recuperación completa
- ✅ Manejo de conexiones intermitentes

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 🐛 Reporte de Problemas

Si encuentras un bug, por favor abre un issue con:
- Descripción del problema
- Pasos para reproducir
- Resultado esperado vs. resultado actual
- Información del entorno (Java version, OS, etc.)

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 👥 Autores

- **Tu Nombre** - *Desarrollo inicial* - [tu-usuario](https://github.com/tu-usuario)

## 🙏 Agradecimientos

- Inspirado en algoritmos clásicos de estructuras de datos
- MongoDB por su excelente documentación
- Comunidad Java por las mejores prácticas

---

⭐ **¡Si este proyecto te fue útil, no olvides darle una estrella!** ⭐
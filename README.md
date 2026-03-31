# BinaryTreeAVL

Sistema de gestion de datos utilizando **Arboles AVL genericos** con persistencia en **MongoDB Atlas**. Implementado en Java con visualizacion en consola y manejo de tipos genericos.

![Java](https://img.shields.io/badge/Java-11+-orange)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue)

## Caracteristicas

- **Arbol AVL generico** (`T extends Comparable<T>`) con auto-balanceo y operaciones en O(log n)
- **Operaciones CRUD** completas: insercion, busqueda, actualizacion y eliminacion
- **Persistencia en MongoDB Atlas** con serializacion automatica via reflexion
- **Visualizacion del arbol** en formato jerarquico ASCII, recorrido inorder y estadisticas
- **Menu interactivo** por consola con validacion de datos

## Requisitos

- **Java** JDK 11 o superior
- **Maven** 3.6.0 o superior
- **MongoDB Atlas** cuenta con cluster configurado
- Conexion a Internet

## Instalacion

### 1. Clonar el repositorio

```bash
git clone https://github.com/Esaban17/BinaryTreeAVL.git
cd BinaryTreeAVL
```

### 2. Configurar MongoDB

Crear un archivo `.env` en la raiz del proyecto (ver `.env.example` como referencia):

```env
MONGODB_URI=mongodb+srv://usuario:password@cluster.mongodb.net/
DATABASE_NAME=avltree_db
COLLECTION_NAME=nodes
```

### 3. Compilar y ejecutar

```bash
mvn clean compile package
java -jar target/binary-tree-avl-1.0.0.jar
```

## Uso

Al ejecutar la aplicacion se presenta un menu interactivo:

```
=== SISTEMA AVL GENERICO ===
1. Insertar persona
2. Actualizar persona
3. Buscar persona por DPI
4. Eliminar persona
5. Graficar arbol
6. Operaciones de base de datos
7. Informacion del arbol
8. Mostrar todas las personas
0. Salir
```

La implementacion de ejemplo usa la clase `Persona`, ordenada automaticamente por DPI (Documento Personal de Identificacion).

### Visualizacion del arbol

```
=== ESTRUCTURA JERARQUICA ===
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

## Estructura del proyecto

```
src/main/java/com/avltree/
├── Main.java                          # Punto de entrada
├── controller/
│   └── AVLTreeController.java         # Controlador del menu interactivo
├── model/
│   ├── Node.java                      # Nodo generico del arbol AVL
│   └── Persona.java                   # Modelo de ejemplo (comparable por DPI)
├── service/
│   ├── AVLTree.java                   # Implementacion del arbol AVL
│   ├── MongoDBConnection.java         # Conexion a MongoDB (Singleton)
│   └── TreePersistenceService.java    # Serializacion y persistencia generica
└── util/
    ├── EnvLoader.java                 # Carga de variables de entorno desde .env
    └── TreeVisualizer.java            # Visualizacion ASCII del arbol
```

## Tecnologias

| Componente   | Tecnologia             | Version |
|-------------|------------------------|---------|
| Lenguaje    | Java                   | 11+     |
| Build       | Maven                  | 3.6+    |
| Base de datos | MongoDB Atlas        | 4.0+    |
| Driver      | MongoDB Java Driver    | 4.11.1  |
| Logging     | SLF4J + Logback        | 1.7.36 / 1.2.12 |
| Testing     | JUnit                  | 5.10.0  |

## Patrones de diseno

- **Singleton**: conexion unica a MongoDB
- **MVC**: separacion controlador / servicio / modelo
- **Repository**: abstraccion de acceso a datos en `TreePersistenceService`
- **Template Method**: serializacion generica mediante reflexion

## Complejidad algoritmica

| Operacion   | Complejidad |
|-------------|-------------|
| Insercion   | O(log n)    |
| Busqueda    | O(log n)    |
| Eliminacion | O(log n)    |
| Recorrido   | O(n)        |

Las rotaciones (simple y doble, izquierda y derecha) mantienen el factor de balance en el rango [-1, 1] garantizando la altura logaritmica del arbol.

## Contribuciones

Las contribuciones son bienvenidas. Consulta [CONTRIBUTING.md](CONTRIBUTING.md) para mas detalles.

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/mi-feature`)
3. Commit tus cambios (`git commit -m 'Agregar mi feature'`)
4. Push a la rama (`git push origin feature/mi-feature`)
5. Abre un Pull Request

## Licencia

Este proyecto esta bajo la Licencia MIT. Ver [LICENSE](LICENSE) para mas detalles.

## Autor

- **Esaban17** - [GitHub](https://github.com/Esaban17)

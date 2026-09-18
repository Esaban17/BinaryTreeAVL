# BinaryTreeAVL

Sistema de gestion de datos utilizando **Arboles AVL genericos** con persistencia en **MongoDB Atlas**. Implementado en Java con visualizacion en consola y manejo de tipos genericos.

[![CI](https://github.com/Esaban17/BinaryTreeAVL/actions/workflows/ci.yml/badge.svg)](https://github.com/Esaban17/BinaryTreeAVL/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-11+-orange)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue)

## Caracteristicas

- **Arbol AVL generico** (`T extends Comparable<T>`) con auto-balanceo y operaciones en O(log n)
- **Operaciones CRUD** completas: insercion, busqueda, actualizacion y eliminacion
- **Persistencia en MongoDB Atlas**, con el identificador de cada documento derivado de la clave natural del dato
- **Visualizacion del arbol** en formato jerarquico ASCII, recorrido inorder y estadisticas
- **Menu interactivo** por consola con validacion de datos
- **76 pruebas unitarias** que cubren el arbol, el modelo, la configuracion y la visualizacion

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
DATABASE_NAME=avltree
COLLECTION_NAME=nodes
```

Cualquiera de estas variables puede definirse tambien como **variable de entorno del sistema**, lo que resulta comodo en Docker o CI. Si una variable existe en el entorno, tiene prioridad sobre el archivo `.env`.

| Variable | Obligatoria | Por defecto | Descripcion |
|----------|:-----------:|-------------|-------------|
| `MONGODB_URI` | Si | — | URI de conexion a MongoDB Atlas |
| `DATABASE_NAME` | No | `avltree` | Nombre de la base de datos |
| `COLLECTION_NAME` | No | `nodes` | Coleccion donde se guardan los nodos |
| `CONNECTION_TIMEOUT` | No | `10000` | Timeout de conexion y seleccion de servidor (ms) |
| `SOCKET_TIMEOUT` | No | `10000` | Timeout de lectura del socket (ms) |
| `MAX_CONNECTION_RETRIES` | No | `5` | Intentos de conexion al arrancar |
| `RETRY_INTERVAL` | No | `2000` | Espera entre intentos (ms) |

El nivel de log no se configura aqui, sino en `src/main/resources/logback.xml`.

### 3. Compilar y ejecutar

```bash
mvn clean verify
java -jar target/binary-tree-avl-1.0.0.jar
```

Tambien puedes ejecutar la aplicacion sin empaquetarla:

```bash
mvn exec:java
```

En Windows, `run.bat` y `run.ps1` verifican los prerequisitos, compilan, ejecutan las pruebas e inician la aplicacion.

## Pruebas

```bash
mvn test
```

La suite cubre:

- **Arbol AVL**: los cuatro casos de rotacion, eliminacion de nodos con 0, 1 y 2 hijos, insercion de claves duplicadas y conflictos de clave al actualizar
- **Prueba de propiedades**: 50 rondas de 400 operaciones aleatorias comparando el arbol contra un `TreeSet`, verificando en cada paso el orden inorder, el factor de balance, las alturas almacenadas y el contador de tamaño
- **Modelo**: ordenamiento y validacion de `Persona`, serializacion de ida y vuelta, y unicidad de los identificadores de persistencia
- **Configuracion**: parseo del `.env` (comentarios, comillas, `export`, valores con `=`) y precedencia de las variables de entorno
- **Visualizacion**: deteccion de desbalances y de alturas incoherentes

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
[Ana Rodriguez (45678901) (h:4)]
├── L: [Maria Gonzalez (23456789) (h:2)]
│   ├── L: [Juan Perez (12345678) (h:1)]
│   └── R: [Carlos Lopez (34567890) (h:1)]
└── R: [Carmen Hernandez (67890123) (h:3)]
    ├── L: [Luis Martinez (56789012) (h:1)]
    └── R: [Miguel Garcia (78901234) (h:2)]
        └── R: [Isabel Ruiz (89012345) (h:1)]
```

## Estructura del proyecto

```
src/main/java/com/avltree/
├── Main.java                          # Punto de entrada
├── controller/
│   └── AVLTreeController.java         # Controlador del menu interactivo
├── model/
│   ├── Node.java                      # Nodo generico del arbol AVL
│   ├── Persona.java                   # Modelo de ejemplo (comparable por DPI)
│   └── PersonaDocumentMapper.java     # Mapeo de Persona a documento de MongoDB
├── service/
│   ├── AVLTree.java                   # Implementacion del arbol AVL
│   ├── DocumentMapper.java            # Contrato de serializacion
│   ├── MongoDBConnection.java         # Conexion a MongoDB (Singleton)
│   └── TreePersistenceService.java    # Persistencia generica del arbol
└── util/
    ├── EnvLoader.java                 # Configuracion desde .env y variables de entorno
    └── TreeVisualizer.java            # Visualizacion ASCII del arbol

src/test/java/com/avltree/             # Pruebas unitarias (JUnit 5)
```

## Como usar el arbol con otro tipo de dato

El arbol es generico: basta con que el tipo implemente `Comparable`. Para persistirlo tambien hay que proporcionar un `DocumentMapper`, que define el identificador del documento y la conversion a BSON:

```java
public class ProductoMapper implements DocumentMapper<Producto> {

    @Override
    public String id(Producto producto) {
        return "producto_" + producto.getCodigo();   // clave natural, nunca hashCode()
    }

    @Override
    public Document toDocument(Producto producto) { /* ... */ }

    @Override
    public Producto fromDocument(Document documento) { /* ... */ }
}

AVLTree<Producto> arbol = new AVLTree<>();
TreePersistenceService<Producto> persistencia =
        new TreePersistenceService<>(arbol, new ProductoMapper());
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

- **Singleton**: conexion unica a MongoDB (modismo del holder estatico, seguro entre hilos)
- **MVC**: separacion controlador / servicio / modelo
- **Repository**: abstraccion de acceso a datos en `TreePersistenceService`
- **Data Mapper**: `DocumentMapper` traduce entre objetos de dominio y documentos

## Complejidad algoritmica

| Operacion   | Complejidad |
|-------------|-------------|
| Insercion   | O(log n)    |
| Busqueda    | O(log n)    |
| Eliminacion | O(log n)    |
| Recorrido   | O(n)        |
| `size()`    | O(1)        |

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

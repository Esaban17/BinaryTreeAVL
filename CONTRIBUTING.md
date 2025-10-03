# 🤝 Guía de Contribución

¡Gracias por tu interés en contribuir al **Sistema AVL Genérico con MongoDB**! Este documento te ayudará a entender cómo participar en el desarrollo del proyecto.

## 📋 Tabla de Contenidos

- [Código de Conducta](#código-de-conducta)
- [Cómo Contribuir](#cómo-contribuir)
- [Configuración del Entorno](#configuración-del-entorno)
- [Estándares de Código](#estándares-de-código)
- [Proceso de Pull Request](#proceso-de-pull-request)
- [Reporte de Issues](#reporte-de-issues)
- [Tipos de Contribuciones](#tipos-de-contribuciones)

## 📜 Código de Conducta

Este proyecto se adhiere a un código de conducta. Al participar, se espera que mantengas un ambiente respetuoso y colaborativo.

### Nuestros Estándares

- **Respeto**: Trata a todos los participantes con respeto y cortesía
- **Inclusión**: Acepta diferentes perspectivas y experiencias
- **Colaboración**: Trabaja de manera constructiva con otros
- **Profesionalismo**: Mantén las discusiones enfocadas en el proyecto

## 🚀 Cómo Contribuir

### 1. Fork del Repositorio
```bash
# Hacer fork en GitHub, luego clonar tu fork
git clone https://github.com/tu-usuario/avl-tree-mongodb.git
cd avl-tree-mongodb

# Agregar el repositorio original como upstream
git remote add upstream https://github.com/repositorio-original/avl-tree-mongodb.git
```

### 2. Crear una Rama
```bash
# Crear rama para tu feature/bugfix
git checkout -b feature/nueva-funcionalidad
# o
git checkout -b bugfix/corregir-problema
# o  
git checkout -b docs/actualizar-documentacion
```

### 3. Realizar Cambios
- Implementa tu funcionalidad o corrección
- Asegúrate de seguir los estándares de código
- Añade tests si es necesario
- Actualiza la documentación

### 4. Commit y Push
```bash
# Commit con mensaje descriptivo
git add .
git commit -m "feat: agregar visualización de árbol en formato JSON"

# Push a tu fork
git push origin feature/nueva-funcionalidad
```

### 5. Crear Pull Request
- Ve a tu fork en GitHub
- Haz clic en "New Pull Request"
- Completa la plantilla de PR
- Espera la revisión

## 🛠️ Configuración del Entorno

### Prerrequisitos
- **Java 17** o superior
- **Maven 3.6+**
- **MongoDB Atlas** (cuenta gratuita)
- **Git**
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)

### Setup Local
```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/avl-tree-mongodb.git
cd avl-tree-mongodb

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus credenciales de MongoDB

# 3. Instalar dependencias
mvn clean install

# 4. Ejecutar tests
mvn test

# 5. Ejecutar la aplicación
mvn exec:java -Dexec.mainClass="com.avltree.Main"
```

### Configuración del IDE

#### IntelliJ IDEA
1. Importar proyecto como proyecto Maven
2. Configurar JDK 17
3. Habilitar anotaciones de procesamiento
4. Instalar plugins: MongoDB, Lombok (si se usa)

#### VS Code
1. Instalar extensiones: Java Extension Pack, MongoDB
2. Configurar workspace settings
3. Usar formatter automático

## 📝 Estándares de Código

### Convenciones de Naming
```java
// Clases: PascalCase
public class AVLTreeController<T> { }

// Métodos y variables: camelCase
public void insertNode(T data) { }
private Node<T> leftChild;

// Constantes: UPPER_SNAKE_CASE
private static final int MAX_RETRIES = 3;

// Packages: lowercase con puntos
package com.avltree.service;
```

### Formato de Código
```java
// Indentación: 4 espacios
public class ExampleClass {
    
    // Línea en blanco después de declaraciones
    private String field;
    
    // Métodos separados por líneas en blanco
    public void method() {
        if (condition) {
            // Hacer algo
        }
    }
    
    // Comentarios descriptivos para lógica compleja
    /**
     * Realiza rotación derecha para balancear el árbol AVL.
     * @param node Nodo que requiere rotación
     * @return Nuevo nodo raíz después de la rotación
     */
    private Node<T> rotateRight(Node<T> node) {
        // Implementación
    }
}
```

### Documentación JavaDoc
```java
/**
 * Implementación genérica de un Árbol AVL autobalanceado.
 * 
 * @param <T> Tipo de datos que debe implementar Comparable
 * @author Tu Nombre
 * @version 1.0
 * @since 1.0
 */
public class AVLTree<T extends Comparable<T>> {
    
    /**
     * Inserta un nuevo elemento en el árbol manteniendo la propiedad AVL.
     *
     * @param data El dato a insertar (no puede ser null)
     * @throws IllegalArgumentException si data es null
     * @throws DuplicateElementException si el elemento ya existe
     */
    public void insert(T data) {
        // Implementación
    }
}
```

### Testing
```java
// Nombres descriptivos de tests
@Test
void shouldBalanceTreeAfterMultipleInsertions() {
    // Given
    AVLTree<Integer> tree = new AVLTree<>();
    
    // When  
    tree.insert(1);
    tree.insert(2);
    tree.insert(3);
    
    // Then
    assertTrue(tree.isBalanced());
    assertEquals(2, tree.getRoot().getData());
}

// Tests para casos edge
@Test
void shouldThrowExceptionWhenInsertingNull() {
    AVLTree<String> tree = new AVLTree<>();
    
    assertThrows(IllegalArgumentException.class, () -> {
        tree.insert(null);
    });
}
```

## 🔄 Proceso de Pull Request

### Checklist Antes de Enviar
- [ ] ✅ **Código compilado**: `mvn clean compile`
- [ ] ✅ **Tests pasando**: `mvn test`
- [ ] ✅ **Documentación actualizada**: JavaDoc, README si es necesario
- [ ] ✅ **Estilo de código**: Siguiendo convenciones del proyecto
- [ ] ✅ **Sin warnings**: Eliminar warnings innecesarios
- [ ] ✅ **Commits organizados**: Commits atómicos con mensajes claros

### Plantilla de Pull Request
```markdown
## 📋 Descripción
Breve descripción de los cambios realizados.

## 🎯 Tipo de Cambio
- [ ] 🐛 Bug fix (cambio que corrige un issue)
- [ ] ✨ Nueva feature (cambio que añade funcionalidad)
- [ ] 💥 Breaking change (fix o feature que causaría que funcionalidad existente no funcione como se espera)
- [ ] 📝 Documentación (cambios solo en documentación)
- [ ] 🔧 Refactoring (cambio de código que no corrige bug ni añade feature)

## 🧪 Testing
- [ ] Los tests existentes pasan
- [ ] Añadí tests para mi código
- [ ] Probé manualmente los cambios

## 📸 Screenshots (si aplica)
<!-- Añadir screenshots de la funcionalidad -->

## ✅ Checklist
- [ ] Mi código sigue el estilo del proyecto
- [ ] Realicé una auto-revisión de mi código
- [ ] Comenté mi código en partes complejas
- [ ] Actualicé la documentación correspondiente
- [ ] Mis cambios no generan nuevos warnings
```

### Proceso de Revisión
1. **Revisión Automática**: CI/CD ejecuta tests y checks
2. **Revisión de Código**: Maintainer revisa el código
3. **Feedback**: Se proporcionan comentarios si es necesario
4. **Iteración**: Hacer cambios basados en feedback
5. **Aprobación**: Una vez aprobado, se hace merge

## 🐛 Reporte de Issues

### Plantilla para Bugs
```markdown
## 🐛 Descripción del Bug
Una descripción clara y concisa del bug.

## 🔄 Pasos para Reproducir
1. Ir a '...'
2. Hacer click en '....'
3. Scroll down to '....'
4. Ver error

## 💭 Comportamiento Esperado
Descripción clara de lo que esperabas que pasara.

## 📸 Screenshots
Si aplica, añadir screenshots para explicar el problema.

## 🖥️ Entorno
 - OS: [e.g. Windows 10, macOS 12, Ubuntu 20.04]
 - Java Version: [e.g. 17.0.2]
 - Maven Version: [e.g. 3.9.5]
 - IDE: [e.g. IntelliJ IDEA 2023.3]

## ℹ️ Información Adicional
Cualquier contexto adicional sobre el problema.
```

### Plantilla para Feature Requests
```markdown
## 🚀 Feature Request

## 📋 Descripción
Una descripción clara y concisa de la feature que te gustaría ver.

## 💡 Motivación
¿Por qué esta feature sería útil? ¿Qué problema resuelve?

## 📝 Solución Propuesta
Una descripción clara de lo que quieres que pase.

## 🔄 Alternativas Consideradas
Una descripción clara de alternativas que has considerado.

## ℹ️ Información Adicional
Cualquier contexto adicional sobre la feature request.
```

## 🎨 Tipos de Contribuciones

### 🐛 **Bug Fixes**
- Corrección de errores existentes
- Mejoras en manejo de excepciones
- Optimizaciones de rendimiento

### ✨ **Nuevas Features**
- Nuevos algoritmos de visualización
- Soporte para nuevos tipos de datos
- Integración con otras bases de datos
- APIs REST para el árbol

### 📝 **Documentación**
- Mejoras en README
- Tutoriales y ejemplos
- Documentación de API
- Traducciones

### 🧪 **Testing**
- Nuevos casos de prueba
- Tests de integración
- Tests de rendimiento
- Cobertura de código

### 🔧 **Herramientas y Configuración**
- Scripts de build
- Configuración de CI/CD
- Docker containers
- Scripts de deployment

## 💡 Ideas para Contribuir

### Principiantes
- [ ] Agregar más ejemplos de uso
- [ ] Mejorar mensajes de error
- [ ] Añadir validaciones de entrada
- [ ] Escribir tests unitarios

### Intermedio
- [ ] Implementar nuevas operaciones del árbol
- [ ] Agregar métricas y logging
- [ ] Optimizar algoritmos existentes
- [ ] Crear visualizaciones gráficas

### Avanzado
- [ ] Soporte para árboles distribuidos
- [ ] Implementar B-Trees o Red-Black Trees
- [ ] Cache inteligente para MongoDB
- [ ] API REST con Spring Boot

## 📞 Contacto

¿Tienes preguntas sobre cómo contribuir?

- 💬 **Discusiones**: Usa GitHub Discussions
- 🐛 **Issues**: Crea un issue en GitHub
- 📧 **Email**: [maintainer@example.com]
- 💭 **Ideas**: Comparte en Discussions

## 🙏 Reconocimientos

Todos los contribuidores serán reconocidos en:
- **README.md**: Lista de contributors
- **CHANGELOG.md**: Créditos por versión
- **Hall of Fame**: Contributors destacados

---

¡Gracias por ayudar a hacer este proyecto mejor! 🎉
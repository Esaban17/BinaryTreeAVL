# Ejemplos de Personas para Probar el Árbol AVL

Este archivo contiene ejemplos de personas que puedes usar para probar la aplicación.

## Datos de Ejemplo

### Persona 1
- **Nombre:** Juan
- **Apellido:** Pérez
- **Edad:** 25
- **DPI:** 12345678

### Persona 2
- **Nombre:** María
- **Apellido:** González
- **Edad:** 30
- **DPI:** 23456789

### Persona 3
- **Nombre:** Carlos
- **Apellido:** López
- **Edad:** 28
- **DPI:** 34567890

### Persona 4
- **Nombre:** Ana
- **Apellido:** Rodríguez
- **Edad:** 22
- **DPI:** 45678901

### Persona 5
- **Nombre:** Luis
- **Apellido:** Martínez
- **Edad:** 35
- **DPI:** 56789012

### Persona 6
- **Nombre:** Carmen
- **Apellido:** Hernández
- **Edad:** 27
- **DPI:** 67890123

### Persona 7
- **Nombre:** Miguel
- **Apellido:** García
- **Edad:** 32
- **DPI:** 78901234

### Persona 8
- **Nombre:** Isabel
- **Apellido:** Ruiz
- **Edad:** 29
- **DPI:** 89012345

## Orden Esperado en el Árbol (por DPI)

Cuando insertes estas personas, el árbol AVL las ordenará automáticamente por DPI:

1. 12345678 - Juan Pérez (25 años)
2. 23456789 - María González (30 años)
3. 34567890 - Carlos López (28 años)
4. 45678901 - Ana Rodríguez (22 años)
5. 56789012 - Luis Martínez (35 años)
6. 67890123 - Carmen Hernández (27 años)
7. 78901234 - Miguel García (32 años)
8. 89012345 - Isabel Ruiz (29 años)

## Pruebas Sugeridas

### 1. Inserción Secuencial
Inserta las personas en el orden dado arriba para ver cómo el árbol se mantiene balanceado.

### 2. Inserción Aleatoria
Inserta las personas en orden aleatorio para ver las rotaciones AVL en acción.

### 3. Búsquedas
- Busca personas por DPI existente: `12345678`
- Busca personas por DPI inexistente: `99999999`

### 4. Actualizaciones
- Actualiza la edad de Juan Pérez a 26 años
- Cambia el nombre de María González a María Fernanda González

### 5. Eliminaciones
- Elimina una persona que sea hoja (sin hijos)
- Elimina una persona que tenga un solo hijo
- Elimina una persona que tenga dos hijos

### 6. Visualización
Después de cada operación importante, usa la opción de "Graficar árbol" para ver:
- La estructura jerárquica del árbol
- El recorrido inorder (debería mostrar personas ordenadas por DPI)
- La información del árbol (altura, balance, etc.)

## Validaciones del Sistema

El sistema validará automáticamente:
- Nombres y apellidos no vacíos
- Edad entre 1 y 149 años
- DPI con al menos 8 caracteres
- No se permiten DPIs duplicados (se actualiza la persona existente)

## Nota sobre MongoDB

Todas las operaciones se guardan automáticamente en MongoDB Atlas si la conexión está configurada correctamente. Puedes usar las opciones del menú de base de datos para:
- Guardar el árbol completo
- Cargar datos desde la base de datos
- Sincronizar datos
- Verificar integridad
- Ver estadísticas

¡Disfruta probando tu árbol AVL de personas!
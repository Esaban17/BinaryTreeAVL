package com.avltree.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvLoaderTest {

    @TempDir
    Path directorioTemporal;

    @BeforeEach
    @AfterEach
    void limpiarEstadoGlobal() {
        EnvLoader.reset();
    }

    private void cargar(String contenido) throws IOException {
        Path archivo = directorioTemporal.resolve(".env");
        Files.write(archivo, contenido.getBytes(StandardCharsets.UTF_8));
        EnvLoader.loadFrom(archivo);
    }

    @Test
    void leeParesClaveValor() throws IOException {
        cargar("DATABASE_NAME=avltree_db\nCOLLECTION_NAME=nodes\n");

        assertEquals("avltree_db", EnvLoader.getEnv("DATABASE_NAME"));
        assertEquals("nodes", EnvLoader.getEnv("COLLECTION_NAME"));
    }

    @Test
    void ignoraComentariosYLineasVacias() throws IOException {
        cargar("# un comentario\n\n   \nDATABASE_NAME=avltree_db\n# otro comentario\n");

        Map<String, String> variables = EnvLoader.snapshot();
        assertEquals(1, variables.size());
        assertEquals("avltree_db", variables.get("DATABASE_NAME"));
    }

    @Test
    @DisplayName("Quita las comillas que envuelven el valor")
    void quitaComillas() throws IOException {
        cargar("CON_DOBLES=\"valor con espacios\"\nCON_SIMPLES='otro valor'\nSIN_COMILLAS=simple\n");

        assertEquals("valor con espacios", EnvLoader.getEnv("CON_DOBLES"));
        assertEquals("otro valor", EnvLoader.getEnv("CON_SIMPLES"));
        assertEquals("simple", EnvLoader.getEnv("SIN_COMILLAS"));
    }

    @Test
    void admiteElPrefijoExport() throws IOException {
        cargar("export DATABASE_NAME=avltree_db\n");

        assertEquals("avltree_db", EnvLoader.getEnv("DATABASE_NAME"));
    }

    @Test
    @DisplayName("Conserva el valor completo aunque contenga signos igual, como una URI de Mongo")
    void conservaLosSignosIgualDelValor() throws IOException {
        String uri = "mongodb+srv://usuario:clave@cluster.mongodb.net/?retryWrites=true&w=majority";
        cargar("MONGODB_URI=" + uri + "\n");

        assertEquals(uri, EnvLoader.getEnv("MONGODB_URI"));
    }

    @Test
    void recortaEspaciosAlrededorDeClaveYValor() throws IOException {
        cargar("  DATABASE_NAME  =   avltree_db   \n");

        assertEquals("avltree_db", EnvLoader.getEnv("DATABASE_NAME"));
    }

    @Test
    void ignoraLineasSinSignoIgual() throws IOException {
        cargar("esto no es una variable\nDATABASE_NAME=avltree_db\n");

        assertEquals(1, EnvLoader.snapshot().size());
        assertEquals("avltree_db", EnvLoader.getEnv("DATABASE_NAME"));
    }

    @Test
    void devuelveElValorPorDefectoCuandoLaVariableNoExiste() throws IOException {
        cargar("DATABASE_NAME=avltree_db\n");

        assertNull(EnvLoader.getEnv("NO_EXISTE"));
        assertEquals("respaldo", EnvLoader.getEnv("NO_EXISTE", "respaldo"));
        assertEquals("avltree_db", EnvLoader.getEnv("DATABASE_NAME", "respaldo"));
        assertFalse(EnvLoader.hasEnv("NO_EXISTE"));
        assertTrue(EnvLoader.hasEnv("DATABASE_NAME"));
    }

    @Test
    void interpretaValoresNumericos() throws IOException {
        cargar("CONNECTION_TIMEOUT=5000\nNO_NUMERICO=abc\n");

        assertEquals(5000, EnvLoader.getEnvAsInt("CONNECTION_TIMEOUT", 10000));
        assertEquals(10000, EnvLoader.getEnvAsInt("NO_NUMERICO", 10000),
                "Un valor no numérico debe caer al valor por defecto, no romper el arranque");
        assertEquals(10000, EnvLoader.getEnvAsInt("NO_DEFINIDO", 10000));
    }

    @Test
    @DisplayName("Una variable de entorno real tiene prioridad sobre el archivo .env")
    void laVariableDeEntornoRealGana() throws IOException {
        // PATH está definida en cualquier entorno donde corran estas pruebas.
        String delSistema = System.getenv("PATH");
        assumeQueExiste(delSistema);

        cargar("PATH=valor-del-archivo\n");

        assertEquals(delSistema.trim(), EnvLoader.getEnv("PATH"));
    }

    @Test
    void noFallaCuandoNoHayArchivoEnv() {
        assertDoesNotThrow(EnvLoader::loadEnv);
    }

    private void assumeQueExiste(String valor) {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                valor != null && !valor.trim().isEmpty(),
                "Se necesita la variable PATH para comprobar la precedencia");
    }
}

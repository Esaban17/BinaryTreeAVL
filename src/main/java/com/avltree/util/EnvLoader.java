package com.avltree.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga de configuración desde un archivo {@code .env}.
 *
 * <p>Las variables de entorno reales tienen prioridad sobre el archivo, de modo
 * que en Docker o CI se puede configurar la aplicación sin escribir un {@code .env}.
 *
 * <p>Formato admitido: {@code CLAVE=valor}, con comentarios que empiezan por
 * {@code #}, prefijo opcional {@code export} y valores entre comillas simples o
 * dobles (las comillas se eliminan).
 */
public final class EnvLoader {

    private static final Logger log = LoggerFactory.getLogger(EnvLoader.class);

    private static final Map<String, String> envVariables = new HashMap<>();
    private static boolean loaded = false;

    private EnvLoader() {
    }

    /**
     * Carga el archivo {@code .env} si aún no se ha cargado.
     *
     * <p>Se busca en el directorio de trabajo y, si no está, junto al jar en
     * ejecución. Si no se encuentra no es un error: puede que toda la
     * configuración venga de variables de entorno.
     */
    public static synchronized void loadEnv() {
        if (loaded) {
            return;
        }
        // Se marca como cargado incluso si falla, para no reintentar ni repetir
        // el mismo mensaje de error en cada consulta.
        loaded = true;

        Path archivo = localizarArchivoEnv();
        if (archivo == null) {
            log.info("No se encontró un archivo .env; se usarán únicamente las variables de entorno");
            return;
        }

        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            for (String linea : lineas) {
                parsearLinea(linea);
            }
            log.info("Configuración cargada desde {} ({} variables)", archivo, envVariables.size());
        } catch (IOException e) {
            log.error("No se pudo leer el archivo {}: {}", archivo, e.getMessage());
        }
    }

    private static Path localizarArchivoEnv() {
        Path enDirectorioActual = Paths.get(".env");
        if (Files.isReadable(enDirectorioActual)) {
            return enDirectorioActual;
        }

        Path juntoAlJar = directorioDelJar();
        if (juntoAlJar != null) {
            Path candidato = juntoAlJar.resolve(".env");
            if (Files.isReadable(candidato)) {
                return candidato;
            }
        }
        return null;
    }

    private static Path directorioDelJar() {
        try {
            Path ubicacion = Paths.get(
                    EnvLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isDirectory(ubicacion) ? ubicacion : ubicacion.getParent();
        } catch (Exception e) {
            return null;
        }
    }

    private static void parsearLinea(String lineaOriginal) {
        String linea = lineaOriginal.trim();
        if (linea.isEmpty() || linea.startsWith("#")) {
            return;
        }
        if (linea.startsWith("export ")) {
            linea = linea.substring("export ".length()).trim();
        }

        int separador = linea.indexOf('=');
        if (separador <= 0) {
            return;
        }

        String clave = linea.substring(0, separador).trim();
        String valor = quitarComillas(linea.substring(separador + 1).trim());
        if (!clave.isEmpty()) {
            envVariables.put(clave, valor);
        }
    }

    private static String quitarComillas(String valor) {
        if (valor.length() >= 2
                && ((valor.startsWith("\"") && valor.endsWith("\""))
                 || (valor.startsWith("'") && valor.endsWith("'")))) {
            return valor.substring(1, valor.length() - 1);
        }
        return valor;
    }

    /**
     * Obtiene una variable de configuración.
     *
     * <p>Una variable de entorno real tiene prioridad sobre el archivo {@code .env}.
     *
     * @return el valor, o null si no está definida
     */
    public static synchronized String getEnv(String key) {
        String delSistema = System.getenv(key);
        if (delSistema != null && !delSistema.trim().isEmpty()) {
            return delSistema.trim();
        }
        loadEnv();
        return envVariables.get(key);
    }

    /**
     * Obtiene una variable de configuración con valor por defecto.
     */
    public static String getEnv(String key, String defaultValue) {
        String value = getEnv(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    /**
     * Obtiene una variable numérica; si no está definida o no es un número
     * válido, devuelve el valor por defecto.
     */
    public static int getEnvAsInt(String key, int defaultValue) {
        String value = getEnv(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("El valor de {} no es un número entero ('{}'); se usa {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Verifica si una variable de configuración está definida.
     */
    public static boolean hasEnv(String key) {
        return getEnv(key) != null;
    }

    /**
     * Carga la configuración desde una ruta concreta, descartando lo cargado
     * previamente. Pensado para pruebas.
     */
    static synchronized void loadFrom(Path archivo) {
        reset();
        loaded = true;
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                parsearLinea(linea);
            }
        } catch (IOException e) {
            log.error("No se pudo leer el archivo {}: {}", archivo, e.getMessage());
        }
    }

    /**
     * Descarta la configuración cargada. Pensado para pruebas.
     */
    static synchronized void reset() {
        envVariables.clear();
        loaded = false;
    }

    /**
     * Vista de solo lectura de las variables leídas del archivo {@code .env}.
     */
    static synchronized Map<String, String> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(envVariables));
    }
}

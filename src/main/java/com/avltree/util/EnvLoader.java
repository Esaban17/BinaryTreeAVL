package com.avltree.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para cargar variables de entorno desde archivo .env
 */
public class EnvLoader {
    private static Map<String, String> envVariables = new HashMap<>();
    private static boolean loaded = false;
    
    /**
     * Carga las variables de entorno desde el archivo .env
     */
    public static void loadEnv() {
        if (loaded) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    envVariables.put(parts[0].trim(), parts[1].trim());
                }
            }
            loaded = true;
            System.out.println("Variables de entorno cargadas exitosamente");
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo .env: " + e.getMessage());
            System.err.println("Asegúrate de que el archivo .env existe en la raíz del proyecto");
        }
    }
    
    /**
     * Obtiene una variable de entorno
     */
    public static String getEnv(String key) {
        if (!loaded) {
            loadEnv();
        }
        return envVariables.get(key);
    }
    
    /**
     * Obtiene una variable de entorno con un valor por defecto
     */
    public static String getEnv(String key, String defaultValue) {
        String value = getEnv(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Verifica si una variable de entorno existe
     */
    public static boolean hasEnv(String key) {
        if (!loaded) {
            loadEnv();
        }
        return envVariables.containsKey(key);
    }
}
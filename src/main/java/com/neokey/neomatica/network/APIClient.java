package com.neokey.neomatica.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.network.OnlineRepository.Repository;
import com.neokey.neomatica.network.OnlineRepository.SchematicInfo;

import net.minecraft.util.math.Vec3i;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente para comunicarse con APIs de repositorios de schematics
 */
public class APIClient {
    
    private final Gson gson;
    private static final int TIMEOUT = 15000; // 15 segundos
    private static final String USER_AGENT = "Neomatica/" + Neomatica.VERSION;
    
    public APIClient() {
        this.gson = new Gson();
    }
    
    /**
     * Busca schematics en un repositorio
     */
    public List<SchematicInfo> search(Repository repository, String query, String category, int maxResults) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
            
            String apiUrl = String.format(
                "%s?query=%s&category=%s&limit=%d",
                repository.getFullApiUrl(),
                encodedQuery,
                encodedCategory,
                maxResults
            );
            
            String response = makeGetRequest(apiUrl);
            return parseSchematicResponse(response, repository.getFormat());
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error en búsqueda API: {}", repository.getName(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene schematics populares de un repositorio
     */
    public List<SchematicInfo> getPopular(Repository repository, String category, int limit) {
        try {
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
            
            String apiUrl = String.format(
                "%s/popular?category=%s&limit=%d",
                repository.getFullApiUrl(),
                encodedCategory,
                limit
            );
            
            String response = makeGetRequest(apiUrl);
            return parseSchematicResponse(response, repository.getFormat());
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error obteniendo populares: {}", repository.getName(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene schematics recientes de un repositorio
     */
    public List<SchematicInfo> getRecent(Repository repository, String category, int limit) {
        try {
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
            
            String apiUrl = String.format(
                "%s/recent?category=%s&limit=%d",
                repository.getFullApiUrl(),
                encodedCategory,
                limit
            );
            
            String response = makeGetRequest(apiUrl);
            return parseSchematicResponse(response, repository.getFormat());
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error obteniendo recientes: {}", repository.getName(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene detalles de un schematic específico
     */
    public SchematicInfo getSchematicDetails(Repository repository, String schematicId) {
        try {
            String apiUrl = String.format(
                "%s/%s",
                repository.getFullApiUrl(),
                schematicId
            );
            
            String response = makeGetRequest(apiUrl);
            List<SchematicInfo> results = parseSchematicResponse(response, repository.getFormat());
            
            return results.isEmpty() ? null : results.get(0);
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error obteniendo detalles: {}", schematicId, e);
            return null;
        }
    }
    
    /**
     * Realiza una petición GET
     */
    private String makeGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    return response.toString();
                }
            } else {
                throw new IOException("HTTP Error: " + responseCode);
            }
            
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Parsea la respuesta JSON de schematics
     */
    private List<SchematicInfo> parseSchematicResponse(String jsonResponse, String format) {
        List<SchematicInfo> results = new ArrayList<>();
        
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            // Intentar diferentes estructuras de respuesta
            JsonArray items = null;
            
            if (root.has("results")) {
                items = root.getAsJsonArray("results");
            } else if (root.has("schematics")) {
                items = root.getAsJsonArray("schematics");
            } else if (root.has("data")) {
                items = root.getAsJsonArray("data");
            } else if (root.isJsonArray()) {
                items = root.getAsJsonArray();
            }
            
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    JsonObject item = items.get(i).getAsJsonObject();
                    SchematicInfo info = parseSchematicItem(item, format);
                    
                    if (info != null) {
                        results.add(info);
                    }
                }
            }
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error parseando respuesta JSON", e);
        }
        
        return results;
    }
    
    /**
     * Parsea un item de schematic individual
     */
    private SchematicInfo parseSchematicItem(JsonObject item, String format) {
        try {
            // ID y nombre (campos requeridos)
            String id = getStringOrDefault(item, "id", "");
            String name = getStringOrDefault(item, "name", "Unnamed");
            
            if (id.isEmpty()) {
                id = String.valueOf(System.currentTimeMillis());
            }
            
            SchematicInfo info = new SchematicInfo(id, name);
            
            // Campos opcionales
            info.setAuthor(getStringOrDefault(item, "author", "Desconocido"));
            info.setDescription(getStringOrDefault(item, "description", ""));
            info.setCategory(getStringOrDefault(item, "category", ""));
            info.setFormat(format);
            
            // Dimensiones
            if (item.has("size")) {
                JsonObject sizeObj = item.getAsJsonObject("size");
                Vec3i size = new Vec3i(
                    getIntOrDefault(sizeObj, "x", 0),
                    getIntOrDefault(sizeObj, "y", 0),
                    getIntOrDefault(sizeObj, "z", 0)
                );
                info.setSize(size);
            } else if (item.has("width") && item.has("height") && item.has("length")) {
                Vec3i size = new Vec3i(
                    item.get("width").getAsInt(),
                    item.get("height").getAsInt(),
                    item.get("length").getAsInt()
                );
                info.setSize(size);
            }
            
            // URLs
            info.setDownloadUrl(getStringOrDefault(item, "download_url", ""));
            info.setPreviewUrl(getStringOrDefault(item, "preview_url", ""));
            info.setThumbnailUrl(getStringOrDefault(item, "thumbnail_url", ""));
            
            // Estadísticas
            info.setDownloads(getIntOrDefault(item, "downloads", 0));
            info.setRating(getFloatOrDefault(item, "rating", 0.0f));
            
            // Fecha
            if (item.has("upload_date")) {
                info.setUploadDate(item.get("upload_date").getAsLong());
            }
            
            return info;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error parseando item de schematic", e);
            return null;
        }
    }
    
    /**
     * Obtiene un string de un JsonObject o retorna un valor por defecto
     */
    private String getStringOrDefault(JsonObject obj, String key, String defaultValue) {
        try {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsString();
            }
        } catch (Exception e) {
            // Ignorar error y retornar valor por defecto
        }
        return defaultValue;
    }
    
    /**
     * Obtiene un int de un JsonObject o retorna un valor por defecto
     */
    private int getIntOrDefault(JsonObject obj, String key, int defaultValue) {
        try {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsInt();
            }
        } catch (Exception e) {
            // Ignorar error y retornar valor por defecto
        }
        return defaultValue;
    }
    
    /**
     * Obtiene un float de un JsonObject o retorna un valor por defecto
     */
    private float getFloatOrDefault(JsonObject obj, String key, float defaultValue) {
        try {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsFloat();
            }
        } catch (Exception e) {
            // Ignorar error y retornar valor por defecto
        }
        return defaultValue;
    }
    
    /**
     * Verifica si una URL es válida
     */
    public boolean isValidUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica la conectividad con un repositorio
     */
    public boolean testConnection(Repository repository) {
        try {
            String response = makeGetRequest(repository.getFullApiUrl() + "/health");
            return !response.isEmpty();
        } catch (Exception e) {
            Neomatica.LOGGER.warn("No se pudo conectar con: {}", repository.getName());
            return false;
        }
    }
}
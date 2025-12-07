package com.neokey.neomatica.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neokey.neomatica.Neomatica;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de repositorios online de schematics
 */
public class OnlineRepository {
    
    private final Gson gson;
    private final SchematicDownloader downloader;
    private final APIClient apiClient;
    private final Map<String, Repository> repositories;
    private final List<String> categories;
    
    private static final String REPOSITORIES_FILE = "config/neomatica/repositories.json";
    
    public OnlineRepository() {
        this.gson = new Gson();
        this.downloader = new SchematicDownloader();
        this.apiClient = new APIClient();
        this.repositories = new HashMap<>();
        this.categories = new ArrayList<>();
        
        loadRepositories();
    }
    
    /**
     * Carga la configuración de repositorios
     */
    private void loadRepositories() {
        File configFile = new File(REPOSITORIES_FILE);
        
        if (!configFile.exists()) {
            Neomatica.LOGGER.warn("Archivo de repositorios no encontrado, usando configuración por defecto");
            loadDefaultRepositories();
            return;
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            
            // Cargar repositorios
            JsonArray reposArray = config.getAsJsonArray("repositories");
            for (int i = 0; i < reposArray.size(); i++) {
                JsonObject repoObj = reposArray.get(i).getAsJsonObject();
                
                Repository repo = new Repository(
                    repoObj.get("name").getAsString(),
                    repoObj.get("url").getAsString(),
                    repoObj.get("api_endpoint").getAsString(),
                    repoObj.get("enabled").getAsBoolean(),
                    repoObj.get("supports_search").getAsBoolean(),
                    repoObj.get("supports_categories").getAsBoolean(),
                    repoObj.get("format").getAsString()
                );
                
                repositories.put(repo.getName(), repo);
            }
            
            // Cargar categorías
            JsonArray categoriesArray = config.getAsJsonArray("categories");
            for (int i = 0; i < categoriesArray.size(); i++) {
                categories.add(categoriesArray.get(i).getAsString());
            }
            
            Neomatica.LOGGER.info("Cargados {} repositorios", repositories.size());
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al cargar repositorios", e);
            loadDefaultRepositories();
        }
    }
    
    /**
     * Carga repositorios por defecto
     */
    private void loadDefaultRepositories() {
        // GrabCraft
        repositories.put("GrabCraft", new Repository(
            "GrabCraft",
            "https://www.grabcraft.com",
            "/api/schematics",
            true,
            true,
            true,
            "litematic"
        ));
        
        // Minecraft Schematics
        repositories.put("Minecraft Schematics", new Repository(
            "Minecraft Schematics",
            "https://www.minecraft-schematics.com",
            "/api/v1/schematics",
            true,
            true,
            true,
            "schematic"
        ));
        
        // Categorías por defecto
        categories.addAll(List.of("all", "buildings", "redstone", "organic", "decoration"));
    }
    
    /**
     * Busca schematics en los repositorios
     */
    public List<SchematicInfo> searchSchematics(String query, String category, int maxResults) {
        List<SchematicInfo> results = new ArrayList<>();
        
        for (Repository repo : repositories.values()) {
            if (!repo.isEnabled() || !repo.supportsSearch()) {
                continue;
            }
            
            try {
                List<SchematicInfo> repoResults = apiClient.search(repo, query, category, maxResults);
                results.addAll(repoResults);
                
                if (results.size() >= maxResults) {
                    break;
                }
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error al buscar en repositorio: {}", repo.getName(), e);
            }
        }
        
        // Limitar resultados
        if (results.size() > maxResults) {
            results = results.subList(0, maxResults);
        }
        
        return results;
    }
    
    /**
     * Obtiene schematics populares
     */
    public List<SchematicInfo> getPopularSchematics(String category, int limit) {
        List<SchematicInfo> results = new ArrayList<>();
        
        for (Repository repo : repositories.values()) {
            if (!repo.isEnabled()) {
                continue;
            }
            
            try {
                List<SchematicInfo> repoResults = apiClient.getPopular(repo, category, limit);
                results.addAll(repoResults);
                
                if (results.size() >= limit) {
                    break;
                }
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error al obtener populares de: {}", repo.getName(), e);
            }
        }
        
        // Limitar resultados
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }
        
        return results;
    }
    
    /**
     * Descarga un schematic
     */
    public boolean downloadSchematic(SchematicInfo schematicInfo) {
        try {
            String downloadUrl = schematicInfo.getDownloadUrl();
            String fileName = schematicInfo.getName() + getFileExtension(schematicInfo.getFormat());
            
            File outputFile = new File("schematics/downloaded", fileName);
            
            return downloader.download(downloadUrl, outputFile);
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al descargar schematic: {}", schematicInfo.getName(), e);
            return false;
        }
    }
    
    /**
     * Obtiene la extensión de archivo según el formato
     */
    private String getFileExtension(String format) {
        return switch (format.toLowerCase()) {
            case "litematic" -> ".litematic";
            case "schem", "sponge" -> ".schem";
            case "schematic", "worldedit" -> ".schematic";
            default -> ".litematic";
        };
    }
    
    /**
     * Obtiene todas las categorías disponibles
     */
    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }
    
    /**
     * Obtiene todos los repositorios
     */
    public Map<String, Repository> getRepositories() {
        return new HashMap<>(repositories);
    }
    
    /**
     * Habilita o deshabilita un repositorio
     */
    public void setRepositoryEnabled(String name, boolean enabled) {
        Repository repo = repositories.get(name);
        if (repo != null) {
            repo.setEnabled(enabled);
        }
    }
    
    /**
     * Clase que representa un repositorio
     */
    public static class Repository {
        private final String name;
        private final String url;
        private final String apiEndpoint;
        private boolean enabled;
        private final boolean supportsSearch;
        private final boolean supportsCategories;
        private final String format;
        
        public Repository(String name, String url, String apiEndpoint, boolean enabled,
                         boolean supportsSearch, boolean supportsCategories, String format) {
            this.name = name;
            this.url = url;
            this.apiEndpoint = apiEndpoint;
            this.enabled = enabled;
            this.supportsSearch = supportsSearch;
            this.supportsCategories = supportsCategories;
            this.format = format;
        }
        
        // Getters
        public String getName() { return name; }
        public String getUrl() { return url; }
        public String getApiEndpoint() { return apiEndpoint; }
        public boolean isEnabled() { return enabled; }
        public boolean supportsSearch() { return supportsSearch; }
        public boolean supportsCategories() { return supportsCategories; }
        public String getFormat() { return format; }
        
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getFullApiUrl() {
            return url + apiEndpoint;
        }
    }
    
    /**
     * Clase que representa información de un schematic
     */
    public static class SchematicInfo {
        private String id;
        private String name;
        private String author;
        private String description;
        private String category;
        private Vec3i size;
        private String downloadUrl;
        private String previewUrl;
        private String thumbnailUrl;
        private String format;
        private int downloads;
        private float rating;
        private long uploadDate;
        
        public SchematicInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        // Getters y Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Vec3i getSize() { return size; }
        public void setSize(Vec3i size) { this.size = size; }
        
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
        
        public String getPreviewUrl() { return previewUrl; }
        public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
        
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public int getDownloads() { return downloads; }
        public void setDownloads(int downloads) { this.downloads = downloads; }
        
        public float getRating() { return rating; }
        public void setRating(float rating) { this.rating = rating; }
        
        public long getUploadDate() { return uploadDate; }
        public void setUploadDate(long uploadDate) { this.uploadDate = uploadDate; }
    }
}
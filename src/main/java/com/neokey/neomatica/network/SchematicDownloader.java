package com.neokey.neomatica.network;

import com.neokey.neomatica.Neomatica;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de descargas de schematics
 */
public class SchematicDownloader {
    
    private final ExecutorService downloadExecutor;
    private final Map<String, DownloadTask> activeDownloads;
    
    private static final int MAX_CONCURRENT_DOWNLOADS = 3;
    private static final int BUFFER_SIZE = 8192;
    private static final int TIMEOUT = 30000; // 30 segundos
    
    public SchematicDownloader() {
        this.downloadExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_DOWNLOADS);
        this.activeDownloads = new ConcurrentHashMap<>();
    }
    
    /**
     * Descarga un schematic de forma síncrona
     */
    public boolean download(String url, File outputFile) {
        try {
            // Crear directorio si no existe
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Realizar descarga
            URL downloadUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("User-Agent", "Neomatica/" + Neomatica.VERSION);
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                long fileSize = connection.getContentLengthLong();
                
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(outputFile)) {
                    
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    long totalBytesRead = 0;
                    
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        
                        // Log de progreso cada 10%
                        if (fileSize > 0 && totalBytesRead % (fileSize / 10) < BUFFER_SIZE) {
                            int progress = (int) ((totalBytesRead * 100) / fileSize);
                            Neomatica.LOGGER.debug("Descarga: {}%", progress);
                        }
                    }
                }
                
                Neomatica.LOGGER.info("Descarga completada: {}", outputFile.getName());
                return true;
                
            } else {
                Neomatica.LOGGER.error("Error HTTP al descargar: {}", responseCode);
                return false;
            }
            
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al descargar schematic", e);
            return false;
        }
    }
    
    /**
     * Descarga un schematic de forma asíncrona
     */
    public void downloadAsync(String url, File outputFile, Consumer<DownloadResult> callback) {
        String taskId = url + "_" + System.currentTimeMillis();
        
        DownloadTask task = new DownloadTask(url, outputFile, callback);
        activeDownloads.put(taskId, task);
        
        downloadExecutor.submit(() -> {
            try {
                boolean success = download(url, outputFile);
                DownloadResult result = new DownloadResult(success, outputFile, null);
                
                if (callback != null) {
                    callback.accept(result);
                }
                
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error en descarga asíncrona", e);
                
                if (callback != null) {
                    DownloadResult result = new DownloadResult(false, outputFile, e.getMessage());
                    callback.accept(result);
                }
            } finally {
                activeDownloads.remove(taskId);
            }
        });
    }
    
    /**
     * Descarga un schematic con seguimiento de progreso
     */
    public void downloadWithProgress(String url, File outputFile, 
                                     Consumer<Integer> progressCallback,
                                     Consumer<DownloadResult> completionCallback) {
        
        downloadExecutor.submit(() -> {
            try {
                File parentDir = outputFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                URL downloadUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);
                connection.setRequestProperty("User-Agent", "Neomatica/" + Neomatica.VERSION);
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    long fileSize = connection.getContentLengthLong();
                    
                    try (InputStream in = connection.getInputStream();
                         FileOutputStream out = new FileOutputStream(outputFile)) {
                        
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        long totalBytesRead = 0;
                        int lastProgress = 0;
                        
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            
                            // Actualizar progreso
                            if (fileSize > 0 && progressCallback != null) {
                                int progress = (int) ((totalBytesRead * 100) / fileSize);
                                if (progress != lastProgress) {
                                    progressCallback.accept(progress);
                                    lastProgress = progress;
                                }
                            }
                        }
                    }
                    
                    if (completionCallback != null) {
                        completionCallback.accept(new DownloadResult(true, outputFile, null));
                    }
                    
                } else {
                    if (completionCallback != null) {
                        completionCallback.accept(new DownloadResult(
                            false, outputFile, "HTTP Error: " + responseCode
                        ));
                    }
                }
                
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error en descarga con progreso", e);
                if (completionCallback != null) {
                    completionCallback.accept(new DownloadResult(false, outputFile, e.getMessage()));
                }
            }
        });
    }
    
    /**
     * Cancela una descarga activa
     */
    public void cancelDownload(String taskId) {
        DownloadTask task = activeDownloads.get(taskId);
        if (task != null) {
            task.cancel();
            activeDownloads.remove(taskId);
        }
    }
    
    /**
     * Cancela todas las descargas activas
     */
    public void cancelAllDownloads() {
        for (DownloadTask task : activeDownloads.values()) {
            task.cancel();
        }
        activeDownloads.clear();
    }
    
    /**
     * Obtiene el número de descargas activas
     */
    public int getActiveDownloadCount() {
        return activeDownloads.size();
    }
    
    /**
     * Verifica si hay descargas activas
     */
    public boolean hasActiveDownloads() {
        return !activeDownloads.isEmpty();
    }
    
    /**
     * Cierra el executor de descargas
     */
    public void shutdown() {
        cancelAllDownloads();
        downloadExecutor.shutdown();
        
        try {
            if (!downloadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                downloadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            downloadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Tarea de descarga
     */
    private static class DownloadTask {
        private final String url;
        private final File outputFile;
        private final Consumer<DownloadResult> callback;
        private volatile boolean cancelled = false;
        
        public DownloadTask(String url, File outputFile, Consumer<DownloadResult> callback) {
            this.url = url;
            this.outputFile = outputFile;
            this.callback = callback;
        }
        
        public void cancel() {
            cancelled = true;
        }
        
        public boolean isCancelled() {
            return cancelled;
        }
        
        public String getUrl() {
            return url;
        }
        
        public File getOutputFile() {
            return outputFile;
        }
    }
    
    /**
     * Resultado de una descarga
     */
    public static class DownloadResult {
        private final boolean success;
        private final File file;
        private final String errorMessage;
        
        public DownloadResult(boolean success, File file, String errorMessage) {
            this.success = success;
            this.file = file;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public File getFile() {
            return file;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
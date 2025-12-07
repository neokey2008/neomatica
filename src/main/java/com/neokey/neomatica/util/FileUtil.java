package com.neokey.neomatica.util;

import com.neokey.neomatica.Neomatica;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utilidades para manejo de archivos
 */
public class FileUtil {
    
    /**
     * Crea un directorio si no existe
     */
    public static boolean createDirectory(String path) {
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                Neomatica.LOGGER.info("Directorio creado: {}", path);
                return true;
            }
            return true;
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al crear directorio: {}", path, e);
            return false;
        }
    }
    
    /**
     * Crea un directorio si no existe (usando File)
     */
    public static boolean createDirectory(File directory) {
        if (directory.exists()) {
            return true;
        }
        
        boolean created = directory.mkdirs();
        if (created) {
            Neomatica.LOGGER.info("Directorio creado: {}", directory.getPath());
        }
        return created;
    }
    
    /**
     * Elimina un archivo
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        try {
            boolean deleted = file.delete();
            if (deleted) {
                Neomatica.LOGGER.info("Archivo eliminado: {}", file.getName());
            }
            return deleted;
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al eliminar archivo: {}", file.getName(), e);
            return false;
        }
    }
    
    /**
     * Elimina un directorio recursivamente
     */
    public static boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return false;
        }
        
        try {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteDirectory(file);
                    }
                }
            }
            return directory.delete();
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al eliminar directorio: {}", directory.getName(), e);
            return false;
        }
    }
    
    /**
     * Copia un archivo
     */
    public static boolean copyFile(File source, File destination) {
        if (source == null || !source.exists() || destination == null) {
            return false;
        }
        
        try {
            // Crear directorio de destino si no existe
            File parentDir = destination.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Neomatica.LOGGER.info("Archivo copiado: {} -> {}", source.getName(), destination.getName());
            return true;
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al copiar archivo", e);
            return false;
        }
    }
    
    /**
     * Mueve un archivo
     */
    public static boolean moveFile(File source, File destination) {
        if (source == null || !source.exists() || destination == null) {
            return false;
        }
        
        try {
            File parentDir = destination.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Neomatica.LOGGER.info("Archivo movido: {} -> {}", source.getName(), destination.getName());
            return true;
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al mover archivo", e);
            return false;
        }
    }
    
    /**
     * Lee todo el contenido de un archivo de texto
     */
    public static String readFileAsString(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al leer archivo: {}", file.getName(), e);
            return null;
        }
    }
    
    /**
     * Escribe contenido a un archivo de texto
     */
    public static boolean writeStringToFile(File file, String content) {
        if (file == null || content == null) {
            return false;
        }
        
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            Files.write(file.toPath(), content.getBytes());
            return true;
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al escribir archivo: {}", file.getName(), e);
            return false;
        }
    }
    
    /**
     * Lista todos los archivos en un directorio
     */
    public static List<File> listFiles(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return new ArrayList<>();
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        
        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                fileList.add(file);
            }
        }
        return fileList;
    }
    
    /**
     * Lista archivos con una extensión específica
     */
    public static List<File> listFilesByExtension(File directory, String extension) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return new ArrayList<>();
        }
        
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(extension.toLowerCase()));
        
        if (files == null) {
            return new ArrayList<>();
        }
        
        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            fileList.add(file);
        }
        return fileList;
    }
    
    /**
     * Lista archivos recursivamente
     */
    public static List<File> listFilesRecursively(File directory) {
        List<File> fileList = new ArrayList<>();
        
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return fileList;
        }
        
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            fileList = paths
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al listar archivos recursivamente", e);
        }
        
        return fileList;
    }
    
    /**
     * Obtiene el tamaño de un archivo en bytes
     */
    public static long getFileSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length();
    }
    
    /**
     * Obtiene el tamaño formateado de un archivo
     */
    public static String getFormattedFileSize(File file) {
        long bytes = getFileSize(file);
        return formatBytes(bytes);
    }
    
    /**
     * Formatea bytes en unidades legibles
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Verifica si un archivo existe
     */
    public static boolean exists(File file) {
        return file != null && file.exists();
    }
    
    /**
     * Obtiene la extensión de un archivo
     */
    public static String getExtension(File file) {
        if (file == null) {
            return "";
        }
        
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        
        if (lastDot > 0 && lastDot < name.length() - 1) {
            return name.substring(lastDot + 1).toLowerCase();
        }
        
        return "";
    }
    
    /**
     * Obtiene el nombre sin extensión
     */
    public static String getNameWithoutExtension(File file) {
        if (file == null) {
            return "";
        }
        
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        
        return name;
    }
    
    /**
     * Comprime archivos en un ZIP
     */
    public static boolean zipFiles(List<File> files, File outputZip) {
        if (files == null || files.isEmpty() || outputZip == null) {
            return false;
        }
        
        try (FileOutputStream fos = new FileOutputStream(outputZip);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            for (File file : files) {
                if (!file.exists()) {
                    continue;
                }
                
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    
                    zos.closeEntry();
                }
            }
            
            Neomatica.LOGGER.info("Archivos comprimidos en: {}", outputZip.getName());
            return true;
            
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al comprimir archivos", e);
            return false;
        }
    }
    
    /**
     * Descomprime un archivo ZIP
     */
    public static boolean unzipFile(File zipFile, File outputDirectory) {
        if (zipFile == null || !zipFile.exists() || outputDirectory == null) {
            return false;
        }
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(outputDirectory, zipEntry.getName());
                
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    File parent = newFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                
                zis.closeEntry();
            }
            
            Neomatica.LOGGER.info("Archivo descomprimido: {}", zipFile.getName());
            return true;
            
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al descomprimir archivo", e);
            return false;
        }
    }
    
    /**
     * Obtiene un nombre de archivo único agregando un sufijo numérico
     */
    public static File getUniqueFile(File file) {
        if (file == null || !file.exists()) {
            return file;
        }
        
        String name = getNameWithoutExtension(file);
        String extension = getExtension(file);
        File parent = file.getParentFile();
        
        int counter = 1;
        File uniqueFile;
        
        do {
            String newName = name + "_" + counter;
            if (!extension.isEmpty()) {
                newName += "." + extension;
            }
            uniqueFile = new File(parent, newName);
            counter++;
        } while (uniqueFile.exists());
        
        return uniqueFile;
    }
    
    /**
     * Crea un archivo temporal
     */
    public static File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix);
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al crear archivo temporal", e);
            return null;
        }
    }
}
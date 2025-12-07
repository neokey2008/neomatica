package com.neokey.neomatica.config;

/**
 * Clase de configuración para Neomatica
 * Almacena todas las opciones configurables del mod
 */
public class NeomaticaConfig {
    
    // Configuración de renderizado
    private boolean showOverlay = true;
    private float overlayOpacity = 0.5f;
    private String colorScheme = "default";
    private boolean renderThroughBlocks = false;
    private boolean showSelectionBox = true;
    
    // Configuración de comportamiento
    private boolean autoImportToLitematica = false;
    private boolean confirmDelete = true;
    private boolean showLayerGuideByDefault = false;
    private boolean showBlockListByDefault = false;
    
    // Configuración de preview 3D
    private boolean enablePreview3D = true;
    private int previewQuality = 2; // 0=Bajo, 1=Medio, 2=Alto
    private boolean autoRotatePreview = false;
    
    // Configuración de descarga
    private String downloadDirectory = "schematics/downloaded";
    private boolean autoDownloadPreviews = true;
    private int maxConcurrentDownloads = 3;
    
    // Configuración de herramientas
    private boolean enableQuickPlace = true;
    private boolean showToolTips = true;
    private String defaultTool = "select";
    
    // Getters y Setters
    
    public boolean isShowOverlay() {
        return showOverlay;
    }
    
    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
    }
    
    public float getOverlayOpacity() {
        return overlayOpacity;
    }
    
    public void setOverlayOpacity(float overlayOpacity) {
        this.overlayOpacity = Math.max(0.0f, Math.min(1.0f, overlayOpacity));
    }
    
    public String getColorScheme() {
        return colorScheme;
    }
    
    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }
    
    public boolean isRenderThroughBlocks() {
        return renderThroughBlocks;
    }
    
    public void setRenderThroughBlocks(boolean renderThroughBlocks) {
        this.renderThroughBlocks = renderThroughBlocks;
    }
    
    public boolean isShowSelectionBox() {
        return showSelectionBox;
    }
    
    public void setShowSelectionBox(boolean showSelectionBox) {
        this.showSelectionBox = showSelectionBox;
    }
    
    public boolean isAutoImportToLitematica() {
        return autoImportToLitematica;
    }
    
    public void setAutoImportToLitematica(boolean autoImportToLitematica) {
        this.autoImportToLitematica = autoImportToLitematica;
    }
    
    public boolean isConfirmDelete() {
        return confirmDelete;
    }
    
    public void setConfirmDelete(boolean confirmDelete) {
        this.confirmDelete = confirmDelete;
    }
    
    public boolean isShowLayerGuideByDefault() {
        return showLayerGuideByDefault;
    }
    
    public void setShowLayerGuideByDefault(boolean showLayerGuideByDefault) {
        this.showLayerGuideByDefault = showLayerGuideByDefault;
    }
    
    public boolean isShowBlockListByDefault() {
        return showBlockListByDefault;
    }
    
    public void setShowBlockListByDefault(boolean showBlockListByDefault) {
        this.showBlockListByDefault = showBlockListByDefault;
    }
    
    public boolean isEnablePreview3D() {
        return enablePreview3D;
    }
    
    public void setEnablePreview3D(boolean enablePreview3D) {
        this.enablePreview3D = enablePreview3D;
    }
    
    public int getPreviewQuality() {
        return previewQuality;
    }
    
    public void setPreviewQuality(int previewQuality) {
        this.previewQuality = Math.max(0, Math.min(2, previewQuality));
    }
    
    public boolean isAutoRotatePreview() {
        return autoRotatePreview;
    }
    
    public void setAutoRotatePreview(boolean autoRotatePreview) {
        this.autoRotatePreview = autoRotatePreview;
    }
    
    public String getDownloadDirectory() {
        return downloadDirectory;
    }
    
    public void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }
    
    public boolean isAutoDownloadPreviews() {
        return autoDownloadPreviews;
    }
    
    public void setAutoDownloadPreviews(boolean autoDownloadPreviews) {
        this.autoDownloadPreviews = autoDownloadPreviews;
    }
    
    public int getMaxConcurrentDownloads() {
        return maxConcurrentDownloads;
    }
    
    public void setMaxConcurrentDownloads(int maxConcurrentDownloads) {
        this.maxConcurrentDownloads = Math.max(1, Math.min(10, maxConcurrentDownloads));
    }
    
    public boolean isEnableQuickPlace() {
        return enableQuickPlace;
    }
    
    public void setEnableQuickPlace(boolean enableQuickPlace) {
        this.enableQuickPlace = enableQuickPlace;
    }
    
    public boolean isShowToolTips() {
        return showToolTips;
    }
    
    public void setShowToolTips(boolean showToolTips) {
        this.showToolTips = showToolTips;
    }
    
    public String getDefaultTool() {
        return defaultTool;
    }
    
    public void setDefaultTool(String defaultTool) {
        this.defaultTool = defaultTool;
    }
}
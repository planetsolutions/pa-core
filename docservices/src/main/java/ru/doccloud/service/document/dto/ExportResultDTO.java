package ru.doccloud.service.document.dto;

import java.io.InputStream;

public class ExportResultDTO {

    private final String exportConfigName;

    private final boolean errorOccured;
    private final String message;

    private final boolean downloadable;
    private final String downloadFileName;
    private final String contentType;
    private final InputStream contentStream;

    public ExportResultDTO(String exportConfigName) {
        this(exportConfigName, false, null, null, null);
    }

    public ExportResultDTO(String exportConfigName, boolean downloadable, String downloadFileName, String contentType, InputStream contentStream) {
        this(exportConfigName, false, null, downloadable, downloadFileName, contentType, contentStream);
    }

    public ExportResultDTO(String exportConfigName, boolean errorOccured, String message) {
        this(exportConfigName, errorOccured, message, false, null, null, null);
    }

    public ExportResultDTO(String exportConfigName, boolean errorOccured, String message, boolean downloadable, String downloadFileName, String contentType, InputStream contentStream) {
        this.exportConfigName = exportConfigName;
        this.downloadable = downloadable;
        this.downloadFileName = downloadFileName;
        this.contentType = contentType;
        this.contentStream = contentStream;
        this.errorOccured = errorOccured;
        this.message = message;
    }

    public String getExportConfigName() {
        return exportConfigName;
    }

    public boolean isDownloadable() {
        return downloadable;
    }

    public String getDownloadFileName() {
        return downloadFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public boolean isErrorOccured() {
        return errorOccured;
    }

    public String getMessage() {
        return message;
    }
}

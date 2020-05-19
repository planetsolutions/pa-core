package ru.doccloud.service;

import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.service.document.dto.ExportResultDTO;

import java.util.Iterator;
import java.util.UUID;

public interface DocumentExportService {

    ExportResultDTO exportSelectedDocuments(SystemDTO exportConfig, String[] fields, UUID[] items);
    ExportResultDTO exportExplicitDocumentObjects(SystemDTO exportConfig, String[] fields, Iterable<DocumentDTO> docIterable);

}

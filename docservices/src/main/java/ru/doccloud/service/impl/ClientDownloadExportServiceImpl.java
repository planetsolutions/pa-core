package ru.doccloud.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.DocumentExportService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.ExportResultDTO;
import ru.doccloud.service.document.dto.SystemDTO;

import java.util.List;
import java.util.UUID;

@Service
@Qualifier("client_export_config")
public class ClientDownloadExportServiceImpl implements DocumentExportService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientDownloadExportServiceImpl.class);

    @Autowired
    DocumentCrudService<DocumentDTO> documentCrudService;

    @Override
    public ExportResultDTO exportSelectedDocuments(SystemDTO exportConfig, String[] fields, UUID[] items) {
        LOGGER.debug("Export of selected documents: config={}, fields={}, items={}", exportConfig, fields, items);
        List<DocumentDTO> docs = documentCrudService.findAllByIds(items, fields);
        return exportDocuments(exportConfig, fields, docs);
    }

    @Override
    public ExportResultDTO exportExplicitDocumentObjects(SystemDTO exportConfig, String[] fields, Iterable<DocumentDTO> docIterable) {
        LOGGER.debug("Export of explicit documents: config={}, iterable={}", exportConfig, docIterable);
        List<DocumentDTO> docs = Lists.newLinkedList(docIterable);
        return exportDocuments(exportConfig, fields, docs);
    }

    private ExportResultDTO exportDocuments(SystemDTO exportConfig, String[] fields, List<DocumentDTO> docs) {
        LOGGER.debug("Got {} documents to export", docs.size());

        String format = exportConfig.getData().get("format").asText();
        switch (format) {
            case "csv":
                return exportCsv(docs, exportConfig, fields);
            case "json":
                return exportJson(docs, exportConfig, fields);
            default:
                throw new UnsupportedOperationException("Unsupported export format: " + format);
        }
    }

    private ExportResultDTO exportCsv(List<DocumentDTO> docs, SystemDTO exportConfig, String[] fields) {
        try {
            String csv = documentCrudService.convertResultToCsv(docs, fields);
            return new ExportResultDTO(
                    exportConfig.getSymbolicName(),
                    true,
                    "export.csv",
                    "application/csv;charset=UTF-8",
                    IOUtils.toInputStream(csv, "UTF-8")
            );
        } catch (Exception e) {
            LOGGER.error("Unable to export CSV", e);
            throw new RuntimeException("Unable to export CSV", e);
        }
    }

    private ExportResultDTO exportJson(List<DocumentDTO> docs, SystemDTO exportConfig, String[] fields) {
        try {
            // TODO: implement property filter to respect fields list provided
            ObjectMapper jsonMapper = new ObjectMapper();
            String json = jsonMapper.writeValueAsString(docs);

            return new ExportResultDTO(
                    exportConfig.getSymbolicName(),
                    true,
                    "export.json",
                    "application/json;charset=UTF-8",
                    IOUtils.toInputStream(json)
            );
        } catch (Exception e) {
            LOGGER.error("Unable to export JSON", e);
            throw new RuntimeException("Unable to export JSON", e);
        }
    }


}

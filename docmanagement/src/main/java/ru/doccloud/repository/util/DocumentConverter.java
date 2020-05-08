package ru.doccloud.repository.util;

import org.jooq.Record;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.model.Document;

import java.util.List;
import java.util.stream.Collectors;

import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;

public class DocumentConverter {

    public static Document convertQueryResultToModelObject(Record queryResult, String[] fields) {
        return  Document.getBuilder(queryResult.getValue(DOCUMENTS.SYS_TITLE))
                .description(queryResult.getValue(DOCUMENTS.SYS_DESC))
                .baseType(queryResult.getValue(DOCUMENTS.SYS_BASE_TYPE))
                .type(queryResult.getValue(DOCUMENTS.SYS_TYPE))
                .id(queryResult.getValue(DOCUMENTS.SYS_UUID))
                .creationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_CR))
                .modificationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_MOD))
                .author(queryResult.getValue(DOCUMENTS.SYS_AUTHOR))
                .modifier(queryResult.getValue(DOCUMENTS.SYS_MODIFIER))
                .filePath(queryResult.getValue(DOCUMENTS.SYS_FILE_PATH))
                .fileMimeType(queryResult.getValue(DOCUMENTS.SYS_FILE_MIME_TYPE))
                .fileLength(queryResult.getValue(DOCUMENTS.SYS_FILE_LENGTH))
                .fileName(queryResult.getValue(DOCUMENTS.SYS_FILE_NAME))
                .fileStorage(queryResult.getValue(DOCUMENTS.SYS_FILE_STORAGE))
                .docVersion(queryResult.getValue(DOCUMENTS.SYS_VERSION))
                .uuid(queryResult.getValue(DOCUMENTS.SYS_UUID))
                .parent(queryResult.getValue(DOCUMENTS.SYS_PARENT_UUID))
                .acl(JsonNodeParser.buildObjectNodeFromRecord(queryResult, "sys_acl"))
                .sourceId(queryResult.getValue(DOCUMENTS.SYS_SOURCE_ID))
                .sourcePackage(queryResult.getValue(DOCUMENTS.SYS_SOURCE_PACKAGE))
                .versionParent(queryResult.getValue(DOCUMENTS.VER_PARENT_UUID))
                .versionSeries(queryResult.getValue(DOCUMENTS.VER_SERIES_UUID))
                .lastVersion(queryResult.getValue(DOCUMENTS.VER_ISLAST))
                .versionComment(queryResult.getValue(DOCUMENTS.VER_COMMENT))
                .data(JsonNodeParser.buildObjectNodeFromRecord(queryResult, fields))
                .build();
    }


    public static Document convertQueryResultToModelObject(DocumentsRecord queryResult) {
        return Document.getBuilder(queryResult.getSysTitle())
                .creationTime(queryResult.getSysDateCr())
                .description(queryResult.getSysDesc())
                .baseType(queryResult.getSysBaseType())
                .type(queryResult.getSysType())
                .data(queryResult.getData())
                .id(queryResult.getSysUuid())
                .modificationTime(queryResult.getSysDateMod())
                .author(queryResult.getSysAuthor())
                .modifier(queryResult.getSysModifier())
                .filePath(queryResult.getSysFilePath())
                .fileMimeType(queryResult.getSysFileMimeType())
                .fileLength(queryResult.getSysFileLength())
                .fileStorage(queryResult.getSysFileStorage())
                .fileName(queryResult.getSysFileName())
                .docVersion(queryResult.getSysVersion())
                .parent(queryResult.getSysParentUuid())
                .uuid(queryResult.getSysUuid())
                .acl(queryResult.getSysAcl())
                .sourceId(queryResult.getSysSourceId())
                .sourcePackage(queryResult.getSysSourcePackage())
                .versionParent(queryResult.getVerParentUuid())
                .versionSeries(queryResult.getVerSeriesUuid())
                .lastVersion(queryResult.getVerIslast())
                .versionComment(queryResult.getVerComment())
                .build();
    }

    public static List<Document> convertQueryResultsToModelObjects(List<DocumentsRecord> queryResults) {

        return queryResults.stream()
                .map(DocumentConverter::convertQueryResultToModelObject)
                .collect(Collectors.toList());
    }

    public static List<Document> convertQueryResults(List<Record> queryResults, String[] fields) {
        return queryResults.stream()
                .map(r-> convertQueryResultToModelObject(r, fields))
                .collect(Collectors.toList());
    }
}

package ru.doccloud.repository.util;

import org.jooq.Record;
import ru.doccloud.document.jooq.db.tables.records.SystemRecord;
import ru.doccloud.document.model.SystemDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.doccloud.document.jooq.db.tables.System.SYSTEM;

public class SystemConverter {
    public static SystemDocument convertQueryResultToModelObject(Record queryResult, String[] fields) {
        return  SystemDocument.getBuilder(queryResult.getValue(SYSTEM.SYS_TITLE))
                .description(queryResult.getValue(SYSTEM.SYS_DESC))
                .type(queryResult.getValue(SYSTEM.SYS_TYPE))
                .id(queryResult.getValue(SYSTEM.SYS_UUID))
                .creationTime(queryResult.getValue(SYSTEM.SYS_DATE_CR))
                .modificationTime(queryResult.getValue(SYSTEM.SYS_DATE_MOD))
                .author(queryResult.getValue(SYSTEM.SYS_AUTHOR))
                .modifier(queryResult.getValue(SYSTEM.SYS_MODIFIER))
                .filePath(queryResult.getValue(SYSTEM.SYS_FILE_PATH))
                .fileName(queryResult.getValue(SYSTEM.SYS_FILE_NAME))
                .uuid(queryResult.getValue(SYSTEM.SYS_UUID))
                .symbolicName(queryResult.getValue(SYSTEM.SYS_SYMBOLIC_NAME))
                .parent(queryResult.getValue(SYSTEM.SYS_PARENT_UUID))
                .data(JsonFromRecordBuilder.buildObjectNodeFromRecord(queryResult, fields))
                .build();
    }


    public static SystemDocument convertQueryResultToModelObject(SystemRecord queryResult) {
        return SystemDocument.getBuilder(queryResult.getSysTitle())
                .creationTime(queryResult.getSysDateCr())
                .description(queryResult.getSysDesc())
                .type(queryResult.getSysType())
                .data(queryResult.getData())
                .id(queryResult.getSysUuid())
                .modificationTime(queryResult.getSysDateMod())
                .author(queryResult.getSysAuthor())
                .modifier(queryResult.getSysModifier())
                .filePath(queryResult.getSysFilePath())
                .fileMimeType(queryResult.getSysFileMimeType())
                .fileLength(queryResult.getSysFileLength())
                .fileName(queryResult.getSysFileName())
                .docVersion(queryResult.getSysVersion())
                .uuid(queryResult.getSysUuid())
                .symbolicName(queryResult.getSysSymbolicName())
                .parent(queryResult.getSysParentUuid())
                .build();
    }

    public static List<SystemDocument> convertQueryResultsToModelObjects(List<SystemRecord> queryResults) {
        return queryResults.stream()
                .map(SystemConverter::convertQueryResultToModelObject)
                .collect(Collectors.toList());
    }

    public static List<SystemDocument> convertQueryResults(List<Record> queryResults, String[] fields) {
        return queryResults.stream()
                .map(r-> convertQueryResultToModelObject(r, fields))
                .collect(Collectors.toList());
    }
}

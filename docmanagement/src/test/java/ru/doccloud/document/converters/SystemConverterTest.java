package ru.doccloud.document.converters;


import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.junit.Test;
import ru.doccloud.document.jooq.db.tables.System;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.jooq.db.tables.records.SystemRecord;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.SystemDocument;
import ru.doccloud.repository.util.DocumentConverter;
import ru.doccloud.repository.util.SystemConverter;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;
import static ru.doccloud.document.jooq.db.tables.System.SYSTEM;

public class SystemConverterTest {

    MockDataProvider provider = new TestDataProvider();
    MockConnection connection = new MockConnection(provider);

    @Test
    public void convertRecordToModelObject_whenRecordNotNUll_thenDocumentCreated(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(SYSTEM).fetch();

        Record record = result.get(0);

        String[] fields = new String[]{"all"};

        SystemDocument systemDocument = SystemConverter.convertQueryResultToModelObject(record, fields);

        assertNotNull(systemDocument);

        String expectedTitle = "title0";
        String expectedDesc = "description0";
        String expectedType = "document0";

        assertEquals(expectedTitle, systemDocument.getTitle());
        assertEquals(expectedDesc, systemDocument.getDescription());
        assertEquals(expectedType, systemDocument.getType());

        // TODO: 5/8/20 add  criterias for all fields
    }

    @Test
    public void convertRecordsToModelObject_RecordsFound_thenListDocsCreated(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(SYSTEM).fetch();

        List<Record> queryResults = (List<Record>) result;

        String[] fields = new String[]{"all"};

        List<SystemDocument> systemDocuments = SystemConverter.convertQueryResults( queryResults, fields);

        assertNotNull(systemDocuments);

        assertEquals(4, systemDocuments.size());

        String expectedTitle = "title";
        String expectedDesc = "description";
        String expectedType = "document";

        for(int i =0; i < systemDocuments.size(); i++){

            SystemDocument systemDocument = systemDocuments.get(i);
            assertEquals(expectedTitle+i, systemDocument.getTitle());
            assertEquals(expectedDesc+i, systemDocument.getDescription());
            assertEquals(expectedType+i, systemDocument.getType());
        }

        // TODO: 5/8/20 add test criterias for all fields 

    }


    @Test
    public void convertDocumentRecordToModelObjectTest(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<SystemRecord> result = create.selectFrom(SYSTEM).fetch();

        SystemRecord record = result.get(0);


        SystemDocument systemDocument = SystemConverter.convertQueryResultToModelObject(record);

        assertNotNull(systemDocument);

        String expectedTitle = "title0";
        String expectedDesc = "description0";
        String expectedType = "document0";

        assertEquals(expectedTitle, systemDocument.getTitle());
        assertEquals(expectedDesc, systemDocument.getDescription());
        assertEquals(expectedType, systemDocument.getType());

        // TODO: 5/8/20 add  criterias for all fields
    }

    @Test
    public void convertDocumentRecordsToModelObjectTest(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<SystemRecord> result = create.selectFrom(SYSTEM).fetch();

        List<SystemRecord> queryResults = result;

        List<SystemDocument> systemDocuments = SystemConverter.convertQueryResultsToModelObjects( queryResults);

        assertNotNull(systemDocuments);

        assertEquals(4, systemDocuments.size());

        String expectedTitle = "title";
        String expectedDesc = "description";
        String expectedType = "document";

        for(int i =0; i < systemDocuments.size()-1; i++){

            SystemDocument systemDocument = systemDocuments.get(i);
            assertEquals(expectedTitle+i, systemDocument.getTitle());
            assertEquals(expectedDesc+i, systemDocument.getDescription());
            assertEquals(expectedType+i, systemDocument.getType());
        }

        // TODO: 5/8/20 add test criterias for all fields
    }






}

package ru.doccloud.document.converters;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.junit.Test;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.model.Document;
import ru.doccloud.repository.util.DocumentConverter;


import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;

public class DocumentConverterTest {

    MockDataProvider provider = new TestDataProvider();
    MockConnection connection = new MockConnection(provider);

    @Test
    public void convertRecordToModelObject_whenRecordNotNUll_thenDocumentCreated(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(0);

        String[] fields = new String[]{"all"};

        Document document = DocumentConverter.convertQueryResultToModelObject(record, fields);

        assertNotNull(document);

        String expectedTitle = "title0";
        String expectedDesc = "description0";
        String expectedType = "document0";

        assertEquals(expectedTitle, document.getTitle());
        assertEquals(expectedDesc, document.getDescription());
        assertEquals(expectedType, document.getBaseType());

        // TODO: 5/8/20 add  criterias for all fields
    }

    @Test
    public void convertRecordsToModelObject_RecordsFound_thenListDocsCreated(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        List<Record> queryResults = (List<Record>) result;

        String[] fields = new String[]{"all"};

        List<Document> documents = DocumentConverter.convertQueryResults( queryResults, fields);

        assertNotNull(documents);
        assertEquals(5, documents.size());

        String expectedTitle = "title";
        String expectedDesc = "description";
        String expectedType = "document";

        for(int i =0; i < documents.size()-1; i++){

            Document document = documents.get(i);
            assertEquals(expectedTitle+i, document.getTitle());
            assertEquals(expectedDesc+i, document.getDescription());
            assertEquals(expectedType+i, document.getBaseType());
        }

        // TODO: 5/8/20 add test criterias for all fields

    }


    @Test
    public void convertDocumentRecordToModelObjectTest(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<DocumentsRecord> result = create.selectFrom(DOCUMENTS).fetch();

        DocumentsRecord record = result.get(0);


        Document document = DocumentConverter.convertQueryResultToModelObject(record);

        assertNotNull(document);

        String expectedTitle = "title0";
        String expectedDesc = "description0";
        String expectedType = "document0";

        assertEquals(expectedTitle, document.getTitle());
        assertEquals(expectedDesc, document.getDescription());
        assertEquals(expectedType, document.getBaseType());

        // TODO: 5/8/20 add  criterias for all fields
    }

    @Test
    public void convertDocumentRecordsToModelObjectTest(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<DocumentsRecord> result = create.selectFrom(DOCUMENTS).fetch();

        List<DocumentsRecord> queryResults = result;

        List<Document> documents = DocumentConverter.convertQueryResultsToModelObjects( queryResults);

        assertNotNull(documents);

        assertEquals(5, documents.size());

        String expectedTitle = "title";
        String expectedDesc = "description";
        String expectedType = "document";

        for(int i =0; i < documents.size()-1; i++){

            Document document = documents.get(i);
            assertEquals(expectedTitle+i, document.getTitle());
            assertEquals(expectedDesc+i, document.getDescription());
            assertEquals(expectedType+i, document.getBaseType());
        }

        // TODO: 5/8/20 add test criterias for all fields
    }






}

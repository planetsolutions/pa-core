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

import static org.junit.Assert.assertNotNull;
import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;

public class DocumentConverterTest {

    MockDataProvider provider = new TestDataProvider();
    MockConnection connection = new MockConnection(provider);

    @Test
    public void convertRecordToModelObjectTest(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(0);

        String[] fields = new String[]{"all"};

        Document document = DocumentConverter.convertQueryResultToModelObject(record, fields);

        assertNotNull(document);

        throw new IllegalStateException("hasn't implemented yet");
    }

    @Test
    public void convertRecordsToModelObjectTest(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        List<Record> queryResults = (List<Record>) result;

        String[] fields = new String[]{"all"};

        List<Document> documents = DocumentConverter.convertQueryResults( queryResults, fields);

        assertNotNull(documents);

        throw new IllegalStateException("hasn't implemented yet");
    }


    @Test
    public void convertDocumentRecordToModelObjectTest(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<DocumentsRecord> result = create.selectFrom(DOCUMENTS).fetch();

        DocumentsRecord record = result.get(0);


        Document document = DocumentConverter.convertQueryResultToModelObject(record);

        assertNotNull(document);

        throw new IllegalStateException("hasn't implemented yet");
    }

    @Test
    public void convertDocumentRecordsToModelObjectTest(){

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<DocumentsRecord> result = create.selectFrom(DOCUMENTS).fetch();

        List<DocumentsRecord> queryResults = result;

        List<Document> documents = DocumentConverter.convertQueryResultsToModelObjects( queryResults);

        assertNotNull(documents);

        throw new IllegalStateException("hasn't implemented yet");
    }






}

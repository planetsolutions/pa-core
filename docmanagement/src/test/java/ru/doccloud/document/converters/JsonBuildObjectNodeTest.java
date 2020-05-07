package ru.doccloud.document.converters;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.junit.Test;
import ru.doccloud.common.util.JsonNodeParser;

import static org.junit.Assert.assertNotNull;
import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;


public class JsonBuildObjectNodeTest {


    MockDataProvider provider = new TestDataProvider();
    MockConnection connection = new MockConnection(provider);


    // TODO: 5/7/20 add tests for all conditions
    @Test
    public void sysAclParseTest() {
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(0);

        ObjectNode aclNode = JsonNodeParser.buildObjectNode(record, "sys_acl");

        assertNotNull(aclNode);

        throw new IllegalStateException("hasn't implemented yet");

    }

    @Test
    public void dataObjectTest(){
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(0);

        String[] fields = new String[]{"all"};

        ObjectNode dataNode = JsonNodeParser.buildObjectNode(record, "data");


        assertNotNull(dataNode);

        throw new IllegalStateException("hasn't implemented yet");

    }



}

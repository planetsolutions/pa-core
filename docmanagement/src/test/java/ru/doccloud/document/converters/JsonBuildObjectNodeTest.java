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
import ru.doccloud.repository.util.JsonFromRecordBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;


public class JsonBuildObjectNodeTest {


    MockDataProvider provider = new TestDataProvider();
    MockConnection connection = new MockConnection(provider);


    @Test
    public void buildObjectNodeFromRecord_WhenSysAclNotNUll_thenNonEmptyJsonReturned() {
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(0);

        ObjectNode aclNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, "sys_acl");

        assertNotNull(aclNode);

        final String expectedSysAcl = "{\"full\":[\"all\"],\"read\":[\"all\"],\"edit_prop\":[\"all\"],\"view_prop\":[\"all\"],\"modify_security\":[\"all\"],\"change_content\":[\"all\"],\"delete\":[\"all\"]}";

        final String actualSysAcl = aclNode.toString();

        assertEquals(expectedSysAcl, actualSysAcl);
    }


    @Test
    public void buildObjectNodeFromRecord_WhenSysAclIsNullStr_thenEmptyJsonReturned() {
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(1);

        ObjectNode aclNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, "sys_acl");

        assertNotNull(aclNode);

        final String expectedSysAcl = "{}";

        final String actualSysAcl = aclNode.toString();

        assertEquals(expectedSysAcl, actualSysAcl);
    }


    @Test
    public void buildObjectNodeFromRecord_WhenSysAclNullStr_thenEmptyJsonReturned() {
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(2);

        ObjectNode aclNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, "sys_acl");

        assertNotNull(aclNode);

        final String expectedSysAcl = "{}";

        final String actualSysAcl = aclNode.toString();

        assertEquals(expectedSysAcl, actualSysAcl);
    }

    @Test
    public void buildObjectFromRecordWithNoFields_whenDataIsNull_thenEmptyJsonReturned(){
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(0);

        String[] fields = new String[0];

        ObjectNode dataNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, fields);

        assertNotNull(dataNode);

        final String expectedData = "{}";

        final String actualData = dataNode.toString();

        assertEquals(expectedData, actualData);

    }

    @Test
    public void buildObjectFromRecordWithAllFieldsAndNullData_whenDataIsNull_thenEmptyJsonReturned(){
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(1);

        String[] fields = new String[]{"all"};

        ObjectNode dataNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, fields);


        assertNotNull(dataNode);

        final String expectedData = "{}";

        final String actualData = dataNode.toString();

        assertEquals(expectedData, actualData);

    }

    @Test
    public void buildObjectFromRecordWithAllFieldsAndNullData_whenDataNullVal_thenEmptyJsonReturned(){
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(2);

        String[] fields = new String[]{"all"};

        ObjectNode dataNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, fields);


        assertNotNull(dataNode);

        final String expectedData = "{}";

        final String actualData = dataNode.toString();

        assertEquals(expectedData, actualData);

    }


    @Test
    public void buildObjectFromRecordWithAllFieldsAndData_whenDataNullVal_thenEmptyJsonReturned(){
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(0);

        String[] fields = new String[]{"all"};

        ObjectNode dataNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, fields);


        assertNotNull(dataNode);

        final String expectedData = "{\"object_type\":\"kc_mission\",\"id\":\"5531089\",\"string_2\":\"\",\"time_stamp\":\"2017-04-13T20:19:23+06:00\",\"string_1\":\"Поручение переведено в состояние \\\"Выполнено\\\"\",\"user_name\":\"coresystem\",\"audited_obj_id\":\"0901b2118003b984\",\"string_5\":\"\",\"owner_name\":\"frolov@rt.ru\",\"event_name\":\"kchistoryevent\",\"string_3\":\"\",\"document_id\":\"0901b2118002cd11\",\"category_name\":\"\",\"user_position\":\"Система\",\"current_state\":\"OnControl\",\"id_2\":\"0000000000000000\",\"id_3\":\"0000000000000000\",\"id_1\":\"0000000000000000\",\"string_4\":\"\",\"id_4\":\"0000000000000000\",\"id_5\":\"0000000000000000\",\"policy_id\":\"4601b21180003716\"}";

        final String actualData = dataNode.toString();

        assertEquals(expectedData, actualData);

    }


    @Test
    public void buildObjectFromRecordWithFields_whenCustomFieldsAndNonJsonVals_thenEmptyJsonReturned(){
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(0);

        String[] fields = new String[]{"sys_title", "sys_desc", "sys_uuid", "sys_parent_uuid"};

        ObjectNode dataNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, fields);


        assertNotNull(dataNode);

        final String expectedData = "{}";

        final String actualData = dataNode.toString();

        assertEquals(expectedData, actualData);

    }


    @Test
    public void buildObjectFromRecordWithFields_whenCustomFieldsAndJsonVals_thenNonEmptyJsonReturned(){
        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        Result<?> result = create.selectFrom(DOCUMENTS).fetch();

        Record record = result.get(4);

        String[] fields = new String[]{"sys_title", "sys_desc"};

        ObjectNode dataNode = JsonFromRecordBuilder.buildObjectNodeFromRecord(record, fields);


        assertNotNull(dataNode);

        final String expectedData = "{\"sys_title\":{\"title\":\"mytitle\"},\"sys_desc\":{\"desc\":\"mydesc\"}}";

        final String actualData = dataNode.toString();

        assertEquals(expectedData, actualData);

    }



}

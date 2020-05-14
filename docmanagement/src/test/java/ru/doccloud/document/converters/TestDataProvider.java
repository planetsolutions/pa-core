package ru.doccloud.document.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.jooq.db.tables.records.SystemRecord;

import java.io.IOException;
import java.util.UUID;

import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;
import static ru.doccloud.document.jooq.db.tables.System.SYSTEM;


public class TestDataProvider implements MockDataProvider {

    private static final String SYS_ACL = "{\"full\":[\"all\"],\"read\":[\"all\"],\"edit_prop\":[\"all\"],\"view_prop\":[\"all\"],\"modify_security\":[\"all\"],\"change_content\":[\"all\"],\"delete\":[\"all\"]}";

    private static final String DATA = "{\"object_type\":\"kc_mission\",\"id\":\"5531089\",\"string_2\":\"\",\"time_stamp\":\"2017-04-13T20:19:23+06:00\",\"string_1\":\"Поручение переведено в состояние \\\"Выполнено\\\"\",\"user_name\":\"coresystem\",\"audited_obj_id\":\"0901b2118003b984\",\"string_5\":\"\",\"owner_name\":\"frolov@rt.ru\",\"event_name\":\"kchistoryevent\",\"string_3\":\"\",\"document_id\":\"0901b2118002cd11\",\"category_name\":\"\",\"user_position\":\"Система\",\"current_state\":\"OnControl\",\"id_2\":\"0000000000000000\",\"id_3\":\"0000000000000000\",\"id_1\":\"0000000000000000\",\"string_4\":\"\",\"id_4\":\"0000000000000000\",\"id_5\":\"0000000000000000\",\"policy_id\":\"4601b21180003716\"}";

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public MockResult[] execute(MockExecuteContext ctx)  {

        JsonNode sys_acl_node = getJsonFromString(SYS_ACL);
        JsonNode data_node = getJsonFromString(DATA);


        DSLContext create = DSL.using(SQLDialect.POSTGRES);
        MockResult[] mock = new MockResult[1];

        // The execute context contains SQL string(s), bind values, and other meta-data
        String sql = ctx.sql();

        if(sql.contains("documents")) {
            Result<DocumentsRecord> result = create.newResult(DOCUMENTS);
            result.add(create.newRecord(DOCUMENTS));
            result.add(create.newRecord(DOCUMENTS));
            result.add(create.newRecord(DOCUMENTS));
            result.add(create.newRecord(DOCUMENTS));
            result.add(create.newRecord(DOCUMENTS));

            result.get(0).setValue(DOCUMENTS.ID, 1);
            result.get(0).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(0).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(0).setValue(DOCUMENTS.SYS_TITLE, "title0");
            result.get(0).setValue(DOCUMENTS.SYS_ACL, sys_acl_node);
            result.get(0).setValue(DOCUMENTS.DATA, data_node);
            result.get(0).setValue(DOCUMENTS.SYS_DESC, "description0");
            result.get(0).setValue(DOCUMENTS.SYS_BASE_TYPE, "document0");

            result.get(1).setValue(DOCUMENTS.ID, 2);
            result.get(1).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(1).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(1).setValue(DOCUMENTS.SYS_TITLE, "title1");
            result.get(1).setValue(DOCUMENTS.SYS_DESC, "description1");
            result.get(1).setValue(DOCUMENTS.SYS_BASE_TYPE, "document1");


            result.get(2).setValue(DOCUMENTS.ID, 3);
            result.get(2).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(2).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(2).setValue(DOCUMENTS.SYS_TITLE, "title2");
            result.get(2).setValue(DOCUMENTS.SYS_ACL, getJsonFromString("null"));
            result.get(2).setValue(DOCUMENTS.DATA, getJsonFromString("null"));
            result.get(2).setValue(DOCUMENTS.SYS_DESC, "description2");
            result.get(2).setValue(DOCUMENTS.SYS_BASE_TYPE, "document2");

            result.get(3).setValue(DOCUMENTS.ID, 4);
            result.get(3).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(3).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(3).setValue(DOCUMENTS.SYS_TITLE, "title3");
            result.get(3).setValue(DOCUMENTS.SYS_ACL, sys_acl_node);
            result.get(3).setValue(DOCUMENTS.SYS_DESC, "description3");
            result.get(3).setValue(DOCUMENTS.SYS_BASE_TYPE, "document3");

            result.get(4).setValue(DOCUMENTS.ID, 5);
            result.get(4).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(4).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(4).setValue(DOCUMENTS.SYS_TITLE, "{\"title\":\"mytitle\"}");
            result.get(4).setValue(DOCUMENTS.SYS_ACL, sys_acl_node);
            result.get(4).setValue(DOCUMENTS.SYS_DESC, "{\"desc\":\"mydesc\"}");
            result.get(4).setValue(DOCUMENTS.SYS_BASE_TYPE, "document");

            mock[0] = new MockResult(1, result);
        } else if (sql.contains("system")){
            Result<SystemRecord> result = create.newResult(SYSTEM);
            result.add(create.newRecord(SYSTEM));
            result.add(create.newRecord(SYSTEM));
            result.add(create.newRecord(SYSTEM));
            result.add(create.newRecord(SYSTEM));

            result.get(0).setValue(SYSTEM.ID, 1);
            result.get(0).setValue(SYSTEM.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(0).setValue(SYSTEM.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(0).setValue(SYSTEM.SYS_TITLE, "title0");
            result.get(0).setValue(SYSTEM.SYS_DESC, "description0");
            result.get(0).setValue(SYSTEM.SYS_TYPE, "document0");

            result.get(1).setValue(SYSTEM.ID, 2);
            result.get(1).setValue(SYSTEM.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(1).setValue(SYSTEM.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(1).setValue(SYSTEM.SYS_TITLE, "title1");
            result.get(1).setValue(SYSTEM.SYS_DESC, "description1");
            result.get(1).setValue(SYSTEM.SYS_TYPE, "document1");


            result.get(2).setValue(SYSTEM.ID, 3);
            result.get(2).setValue(SYSTEM.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(2).setValue(SYSTEM.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(2).setValue(SYSTEM.SYS_TITLE, "title2");
            result.get(2).setValue(SYSTEM.SYS_ACL, getJsonFromString("null"));
            result.get(2).setValue(SYSTEM.DATA, getJsonFromString("null"));
            result.get(2).setValue(SYSTEM.SYS_DESC, "description2");
            result.get(2).setValue(SYSTEM.SYS_TYPE, "document2");

            result.get(3).setValue(SYSTEM.ID, 4);
            result.get(3).setValue(SYSTEM.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
            result.get(3).setValue(SYSTEM.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
            result.get(3).setValue(SYSTEM.SYS_TITLE, "title3");
            result.get(3).setValue(SYSTEM.SYS_ACL, sys_acl_node);
            result.get(3).setValue(SYSTEM.SYS_DESC, "description3");
            result.get(3).setValue(SYSTEM.SYS_TYPE, "document3");

            mock[0] = new MockResult(1, result);
        }

        return mock;
    }

    private JsonNode getJsonFromString(String strToJson){
        JsonNode node = null;

        try {
            node = mapper.readTree(strToJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return node;
    }
}

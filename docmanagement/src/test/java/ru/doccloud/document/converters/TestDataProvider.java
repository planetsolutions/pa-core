package ru.doccloud.document.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;


public class TestDataProvider implements MockDataProvider {

    private static final String SYS_ACL = "{\"full\":[\"all\"],\"read\":[\"all\"],\"edit_prop\":[\"all\"],\"view_prop\":[\"all\"],\"modify_security\":[\"all\"],\"change_content\":[\"all\"],\"delete\":[\"all\"]}";

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public MockResult[] execute(MockExecuteContext ctx)  {

        JsonNode sys_acl_node = getJsonFromString(SYS_ACL);


        DSLContext create = DSL.using(SQLDialect.POSTGRES);
        MockResult[] mock = new MockResult[1];

        // The execute context contains SQL string(s), bind values, and other meta-data
//        String sql = ctx.sql();


        Result<DocumentsRecord> result =  create.newResult(DOCUMENTS);
        result.add( create.newRecord(DOCUMENTS));
        result.add( create.newRecord(DOCUMENTS));
        result.add( create.newRecord(DOCUMENTS));
        result.add( create.newRecord(DOCUMENTS));

        result.get(0).setValue(DOCUMENTS.ID, 3);
        result.get(0).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
        result.get(0).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
        result.get(0).setValue(DOCUMENTS.SYS_TITLE, "title");
        result.get(0).setValue(DOCUMENTS.SYS_ACL, sys_acl_node);
        result.get(0).setValue(DOCUMENTS.SYS_DESC, "description");
        result.get(0).setValue(DOCUMENTS.SYS_BASE_TYPE, "document");

        result.get(1).setValue(DOCUMENTS.ID, 3);
        result.get(1).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
        result.get(1).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
        result.get(1).setValue(DOCUMENTS.SYS_TITLE, "title");
        result.get(1).setValue(DOCUMENTS.SYS_ACL, sys_acl_node);
        result.get(1).setValue(DOCUMENTS.SYS_DESC, "description");
        result.get(1).setValue(DOCUMENTS.SYS_BASE_TYPE, "document");


        result.get(2).setValue(DOCUMENTS.ID, 3);
        result.get(2).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
        result.get(2).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
        result.get(2).setValue(DOCUMENTS.SYS_TITLE, "title");
        result.get(2).setValue(DOCUMENTS.SYS_ACL, sys_acl_node);
        result.get(2).setValue(DOCUMENTS.SYS_DESC, "description");
        result.get(2).setValue(DOCUMENTS.SYS_BASE_TYPE, "document");

        result.get(3).setValue(DOCUMENTS.ID, 3);
        result.get(3).setValue(DOCUMENTS.SYS_UUID, UUID.fromString("1e1d16c9-bbd8-4ce3-8d77-28082a8bd59e"));
        result.get(3).setValue(DOCUMENTS.SYS_PARENT_UUID, UUID.fromString("c33cae4-9847-479a-859b-5ff6cb0b45b0"));
        result.get(3).setValue(DOCUMENTS.SYS_TITLE, "title");
        result.get(3).setValue(DOCUMENTS.SYS_ACL, sys_acl_node);
        result.get(3).setValue(DOCUMENTS.SYS_DESC, "description");
        result.get(3).setValue(DOCUMENTS.SYS_BASE_TYPE, "document");



        mock[0] = new MockResult(1, result);

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

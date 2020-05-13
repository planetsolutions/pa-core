package ru.doccloud.docs;

import ru.doccloud.common.CommonTest;
import ru.doccloud.document.model.AbstractDocument;
import ru.doccloud.service.document.dto.AbstractDocumentDTO;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public abstract class CommonDocTest extends CommonTest {
    public <T> void assertCriteria(T t, Map<String, Object> expectedValues){

        assertNotNull(t);
        List<Field> fieldList = getFields(t);
        expectedValues.forEach((k,v) ->
        {
            try {
                Field field = fieldList.stream().filter(f -> f.getName().equals(k)).findAny().orElseThrow(NoSuchFieldException:: new);

                field.setAccessible(true);
                if(field.get(t) != null) {
                    assertEquals(v, field.get(t));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public Map<String, Object> buildExpectedValuesMap(List<String> fields, List<Object> values) {

        return IntStream.range(0, fields.size())
                .boxed()
                .collect(Collectors.toMap(fields::get, values::get));
    }


    private <T> List<Field> getFields(T t) {
        List<Field> fields = new ArrayList<>();
        Class clazz = t.getClass();
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

}

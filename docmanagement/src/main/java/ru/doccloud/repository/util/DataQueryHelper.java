package ru.doccloud.repository.util;

import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jooq.Condition;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.SortField;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.doccloud.document.model.FilterBean;
import ru.doccloud.document.model.QueryParam;

public class DataQueryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataQueryHelper.class);




    public static Field<Object> getFieldValue(QueryParam param) {
        Field<Object> result = null;
        DataType<Object> JSONB = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "jsonb");
        DataType<Object> intType = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "int");
        DataType<Object> timeType = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "timestamp");
        DataType<Object> textType = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "text");

        try {
            java.lang.reflect.Field tableField = DOCUMENTS.getClass().getField(param.getField().toUpperCase());
            Field<Object> sortField = (TableField) tableField.get(DOCUMENTS);
            LOGGER.trace("getFieldValue(): Field {}, type {}", sortField, sortField.getDataType());

//            if (sortField.getDataType().isNumeric()){
//                LOGGER.trace("getFieldValue(): integer");
//                result = DSL.val(param.getValue()).cast(intType);
//            }else 
            if(sortField.getDataType().isDateTime()){
                LOGGER.info("getFieldValue(): Timestamp");
                result = DSL.val(Timestamp.valueOf(param.getValue())).cast(sortField.getDataType());
            }else{
                result = DSL.val(param.getValue()).cast(sortField.getDataType());
            }

        } catch (NoSuchFieldException | IllegalAccessException ex) {
            LOGGER.trace("getFieldValue(): Could not find table field: {}", param);
            //result =  DSL.val(param.getValue());
            //try {
            //	Double intval = Double.parseDouble(param.getValue());
            //}catch (Exception exN){
            if ("integer".equals(param.getType())||"number".equals(param.getType())){
                result =  DSL.val(param.getValue()).cast(JSONB);
                LOGGER.trace("getFieldValue(): Cast to JSONB");
            }else if("date-time".equals(param.getType())){
            	result =  DSL.val(param.getValue().replace(" ", "T")).cast(textType);
            }else{
                result =  DSL.val(param.getValue()).cast(textType);
            }

        }
        LOGGER.trace("getFieldValue(): Result {}", result);
        return result;
    }


    public static SortField<?> convertTableFieldToSortField(Field<Object> tableField, Sort.Direction sortDirection) {
        if (sortDirection == Sort.Direction.ASC) {
            return tableField.asc();
        }
        else {
            return tableField.desc();
        }
    }


    public static Collection<SortField<?>> getSortFields(Sort sortSpecification, TableImpl<?> table, Field<?> field, Map<String, String> propTypes) {
        LOGGER.trace("entering getSortFields(sortSpecification={})", sortSpecification);
        Collection<SortField<?>> querySortFields = new ArrayList<>();

        if (sortSpecification == null) {
            LOGGER.trace("getSortFields(): No sort specification found. Returning empty collection -> no sorting is done.");
            return querySortFields;
        }

        for (Sort.Order specifiedField : sortSpecification) {
            String sortFieldName = specifiedField.getProperty();
            Sort.Direction sortDirection = specifiedField.getDirection();
            LOGGER.trace("getSortFields(): Getting sort field with name: {} and direction: {}", sortFieldName, sortDirection);
            boolean textField = true;
            if (propTypes!=null){
	            if ("integer".equals(propTypes.get(sortFieldName))||"number".equals(propTypes.get(sortFieldName))){
	            	textField = false;
	            }
            }
            Field<Object> tableField = getTableField(sortFieldName, table, field, textField);
            SortField<?> querySortField = DataQueryHelper.convertTableFieldToSortField(tableField, sortDirection);

            LOGGER.trace("getSortFields(): tableField: {} and querySortField: {}", tableField, querySortField);
            querySortFields.add(querySortField);
        }

        LOGGER.trace("leaving getSortFields(): querySortFields {}", querySortFields);

        return querySortFields;
    }

    public static Condition createWhereConditions(String likeExpression, Field<?> fieldDesc, Field<?> fieldTitle) {
        return fieldDesc.likeIgnoreCase(likeExpression)
                .or(fieldTitle.likeIgnoreCase(likeExpression));
    }

    public static Field<Object> getTableField(String sortFieldName, TableImpl<?> table, Field<?> field, boolean textoperand) {
        LOGGER.trace("entering getTableField(sortFieldName={})", sortFieldName);
        Field<Object> sortField = null;
        try {
            java.lang.reflect.Field tableField = table.getClass().getField(sortFieldName.toUpperCase());
            sortField = (TableField) tableField.get(table);
            LOGGER.trace("getTableField(): sortField - {}", sortField);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            LOGGER.trace("getTableField(): Could not find table field: {}, Try to search in JSON data", sortFieldName);
            if (textoperand){
            	sortField = jsonText(field, sortFieldName);
            }else{
            	sortField = jsonObject(field, sortFieldName);
            }
            LOGGER.trace("getTableField(): sort field in  JSON data", sortField);
        }

        LOGGER.trace("leaving getTableField()", sortField);
        return sortField;
    }

    public static Field<Object> getFilterField(QueryParam param, TableImpl<?> table, Field<?> field, boolean textoperand) {
        Field<Object> sortField = null;
        try {
            java.lang.reflect.Field tableField = table.getClass().getField(param.getField().toUpperCase());
            sortField = (TableField) tableField.get(table);
            LOGGER.trace("getFilterField(): sortField - {}", sortField);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
        	if (textoperand){
        		sortField = jsonText(field, param.getField());
        	}else{
        		if ("integer".equals(param.getType())||"number".equals(param.getType())){
	                sortField = jsonObject(field, param.getField());
	            }else{
	                sortField = jsonText(field, param.getField());
	            }
        	}
            LOGGER.trace("getFilterField(): sort field in  JSON data", sortField);
        }

        LOGGER.trace("leaving getTableField()", sortField);
        return sortField;
    }

    public static Field<Object> jsonObject(Field<?> field, String name) {
        return DSL.field("{0}->{1}", Object.class, field, DSL.inline(name));
    }

    private static Field<Object> jsonText(Field<?> field, String name) {
        return DSL.field("{0}->>{1}", Object.class, field, DSL.inline(name));
    }

    public static Condition extendConditions(Condition cond, List<QueryParam> queryParams, TableImpl<?> table, Field<?> field) {
        if (queryParams !=null)
            for (QueryParam param : queryParams) {
                LOGGER.trace("extendConditions: Param {} {} {} ",param.getField(),param.getOperand(),param.getValue());
                if (param.getOperand()!=null){

//        	    // ['eq','ne','lt','le','gt','ge','bw','bn','in','ni','ew','en','cn','nc']
//                    todo rewrite using enum implementation
                    final String operand = param.getOperand().toLowerCase();

                    LOGGER.trace("extendConditions: operand ",operand);
                    switch (operand)
                    {
                        case "eq":
                            cond = cond.and(getFilterField(param, table, field,false).equal(getFieldValue(param)));
                            break;
                        case "ne":
                            cond = cond.and(getFilterField(param, table, field,false).notEqual(getFieldValue(param)));
                            break;
                        case "lt":
                            cond = cond.and(getFilterField(param, table, field,false).lessThan(getFieldValue(param)));
                            break;
                        case "le":
                            cond = cond.and(getFilterField(param, table, field,false).lessOrEqual(getFieldValue(param)));
                            break;
                        case "gt":
                            cond = cond.and(getFilterField(param, table, field,false).greaterThan(getFieldValue(param)));
                            break;
                        case "ge":
                            cond = cond.and(getFilterField(param, table, field,false).greaterOrEqual(getFieldValue(param)));
                            break;
                        case "bw":
                            cond = cond.and(getFilterField(param, table, field,true).likeIgnoreCase(param.getValue()+"%"));
                            break;
                        case "bn":
                            cond = cond.and(getFilterField(param, table, field,true).notLikeIgnoreCase(param.getValue()+"%"));
                            break;
                        case "in":
                            cond = cond.and(getFilterField(param, table, field,true).in(getFieldValue(param)));
                            break;
                        case "ni":
                            cond = cond.and(getFilterField(param, table, field,true).notIn(getFieldValue(param)));
                            break;
                        case "ew":
                            cond = cond.and(getFilterField(param, table, field,true).likeIgnoreCase("%"+param.getValue()));
                            break;
                        case "en":
                            cond = cond.and(getFilterField(param, table, field,true).notLikeIgnoreCase("%"+param.getValue()));
                            break;
                        case "cn":
                            cond = cond.and(getFilterField(param, table, field,true).likeIgnoreCase("%"+param.getValue()+"%"));
                            break;
                        case "nc":
                            cond = cond.and(getFilterField(param, table, field,true).notLikeIgnoreCase("%"+param.getValue()+"%"));
                            break;
                        case "nu":
                            cond = cond.and(getFilterField(param, table, field,true).isNull());
                            break;
                        case "nn":
                            cond = cond.and(getFilterField(param, table, field,true).isNotNull());
                            break;
                    }
                }
            }
        return cond;
    }

    public static Map<String, String> getPropertiesType(JsonNode typeData) {
	    Map<String, String> res = new HashMap<String, String>();
	    JsonNode schemaNode = typeData.get("schema");
	    if (!schemaNode.isNull()){
	    	try{
	    	Iterator<Entry<String, JsonNode>> iter = schemaNode.get("properties").fields();
	    	while (iter.hasNext()) {
	            Map.Entry<String, JsonNode> entry = iter.next();
	             	LOGGER.trace("getPropertiesType(): Param {} has type {}",entry.getKey(), entry.getValue().get("type").textValue());
	            	if ("string".equals(entry.getValue().get("type").textValue()) && entry.getValue().get("format")!=null && "date-time".equals(entry.getValue().get("format").textValue())){
	            		res.put(entry.getKey(), entry.getValue().get("format").textValue());
	            	}else{
	            		res.put(entry.getKey(), entry.getValue().get("type").textValue());
	            	}
	            }
	    	}catch(Exception e) {
	    		LOGGER.error("getPropertiesType(): Error getting param type",e);
	            e.printStackTrace();
	    	}
	    }
	    return res;
    }

    public static List<QueryParam> getQueryParams(String query, Map<String, String> attTypes) {
        LOGGER.trace("entering getQueryParams(query={})", query);
        FilterBean filter = null;
        List<QueryParam> queryParams = null;

        ObjectMapper mapper = new ObjectMapper();
        if (query!=null && !"".equals(query)){
            try {
                filter = mapper.readValue(query, new TypeReference<FilterBean>(){});
                queryParams = filter.getMrules();
                LOGGER.trace("getQueryParams(): List of params - {} {}", queryParams.toString(), queryParams.size());
                if (attTypes!=null){
                	for (QueryParam param : queryParams) {
                		param.setType(attTypes.get(param.getField()));
                		LOGGER.trace("getQueryParams(): Param {} has type {}",param.getField(), param.getType());
                	}
                }
                LOGGER.trace("getQueryParams(): Typed params - {} {}", queryParams.toString(), queryParams.size());
            } catch (IOException e) {
                LOGGER.error("getQueryParams(): Error parsing JSON {}",e);
                e.printStackTrace();
            }
        }

        LOGGER.trace("leaving  getQueryParams() : queryParams {}", queryParams);
        return queryParams;
    }
}

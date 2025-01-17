package com.bullhornsdk.data.api.helper;

import com.bullhornsdk.data.api.helper.json.DynamicNullValueFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.bullhornsdk.data.exception.RestMappingException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class RestJsonConverter {

    private static Logger log = LogManager.getLogger(RestJsonConverter.class);

    private final ObjectMapper objectMapper;

    public RestJsonConverter() {
        this.objectMapper = createObjectMapper();
    }

    /*
     * **************************************************************************************************************
     * json/java conversion
     * **************************************************************************************************************
     */

    /**
     * Create the ObjectMapper that deserializes entity to json String.
     *
     * Registers the JodaModule to convert DateTime so-called epoch timestamp (number of milliseconds since January 1st, 1970,
     * UTC)
     *
     * @return
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setFilterProvider(createFieldFilter(Collections.emptySet()));
        return mapper;
    }

    public <T> T jsonToEntity(String jsonString, Class<T> type) {
        try {
            return this.objectMapper.readValue(jsonString, type);
        } catch(JsonParseException e) {
            throw new RestMappingException("Error mapping jsonString to " + type + ". jsonString = " + jsonString, e);
        } catch(JsonMappingException e) {
            throw new RestMappingException("Error mapping jsonString to " + type + ". jsonString = " + jsonString, e);
        } catch(IOException e) {
            throw new RestMappingException("Error mapping jsonString to " + type + ". jsonString = " + jsonString, e);
        }
    }

    /**
     * Takes a BullhornEntity and converts it to a String in json format.
     *
     * @param entity
     *            a BullhornEntity
     * @return the jsonString
     */
    public <T extends BullhornEntity> String convertEntityToJsonString(T entity) {
        String jsonString = "";
        try {
            jsonString = this.objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing entity of type" + entity.getClass() + " to jsonString.", e);
        }
        return jsonString;
    }

    /**
     * Takes a BullhornEntity and converts it to a String in json format, optionally including null values.
     *
     * @param entity
     *            a BullhornEntity
     * @param nullBypassFields fields to include regardless of whether they are null.
     * @return the jsonString
     */
    public <T extends BullhornEntity> String convertEntityToJsonString(T entity, Set<String> nullBypassFields) {
        String jsonString = "";
        try {
            jsonString = this.objectMapper.writer(createFieldFilter(nullBypassFields)).writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing entity of type" + entity.getClass() + " to jsonString.", e);
        }
        return jsonString;
    }

    private FilterProvider createFieldFilter(Set<String> nullBypassFields) {
        return new SimpleFilterProvider().addFilter(DynamicNullValueFilter.FILTER_NAME, new DynamicNullValueFilter(nullBypassFields));
    }
}

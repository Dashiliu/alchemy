/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.dfire.platform.alchemy.connectors.elasticsearch;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.CompositeType;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author congbai
 * @date 05/06/2018
 */
public class JsonRowStringSchema implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(JsonRowStringSchema.class);

    private static final String KVMAP = "kvmap";
    private static final String DATEFORMAT = "dateformat";
    private static final String TIMESTAMP = "@timestamp";
    private static final String FORMAT = "yyyy-MM-dd,HH:mm:ss.SSS";
    /**
     * Object mapper that is used to create output JSON objects.
     */
    private static ObjectMapper mapper = new ObjectMapper();
    /**
     * Fields names in the input Row object.
     */
    private final String[] fieldNames;

    /**
     * Creates a JSON serialization schema for the given fields and types.
     *
     * @param rowSchema The schema of the rows to encode.
     */
    public JsonRowStringSchema(RowTypeInfo rowSchema) {

        Preconditions.checkNotNull(rowSchema);
        String[] fieldNames = rowSchema.getFieldNames();
        TypeInformation[] fieldTypes = rowSchema.getFieldTypes();

        // check that no field is composite
        for (int i = 0; i < fieldTypes.length; i++) {
            if (fieldTypes[i] instanceof CompositeType) {
                throw new IllegalArgumentException("JsonRowSerializationSchema cannot encode rows with nested schema, "
                        + "but field '" + fieldNames[i] + "' is nested: " + fieldTypes[i].toString());
            }
        }

        this.fieldNames = fieldNames;
    }

    public String serialize(Row row) {
        if (row.getArity() != fieldNames.length) {
            throw new IllegalStateException(
                    String.format("Number of elements in the row %s is different from number of field names: %d", row,
                            fieldNames.length));
        }

        ObjectNode objectNode = mapper.createObjectNode();

        for (int i = 0; i < row.getArity(); i++) {
            //值为null的字段不需存es，无意义
            if (row.getField(i) == null) {
                continue;
            }
            JsonNode node = mapper.valueToTree(row.getField(i));
            //如果字段名称是以kvmap开头，则单独处理
            if (fieldNames[i].startsWith(KVMAP)) {
                Map<String, Object> map = (Map<String, Object>) row.getField(i);
                if (map != null) {
                    map.forEach((k, v) -> {
                        objectNode.set(k, mapper.valueToTree(v));
                    });
                }
            } else if (fieldNames[i].equals(DATEFORMAT)) {
                try {
                    Date date = new SimpleDateFormat(FORMAT).parse(row.getField(i).toString());
                    objectNode.set(TIMESTAMP, mapper.valueToTree(date));
                } catch (ParseException e) {
                    logger.error("timestamp is fail",e);
                    objectNode.set(TIMESTAMP, mapper.valueToTree(new Date()));
                }

            } else {
                objectNode.set(fieldNames[i], node);
            }
        }

        try {
            return mapper.writeValueAsString(objectNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize row", e);
        }
    }

    public String[] getFieldNames() {
        return this.fieldNames;
    }
}

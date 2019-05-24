package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.ClusterType;
import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.Field;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.table.sources.CsvTableSource;
import org.apache.flink.table.typeutils.TypeStringUtils;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author congbai
 * @date 2019/5/24
 */
public class CsvConnectorDescriptor implements ConnectorDescriptor{

    private String path;

    private String fieldDelim;

    private String rowDelim;

    private Character quoteCharacter;

    private boolean ignoreFirstLine;

    private String ignoreComments;

    private boolean lenient;

    @Override
    public <T> T buildSource(ClusterType clusterType, List<Field> schema, FormatDescriptor format) throws Exception {
        if (ClusterType.FLINK.equals(clusterType)) {
            return buildCsvFlinkSource(schema, format);
        }
        throw new UnsupportedOperationException("unknow clusterType:" + clusterType);
    }

    private <T> T buildCsvFlinkSource(List<Field> schema, FormatDescriptor format) {
        String[] columnNames = new String[schema.size()];
        TypeInformation[] columnTypes = new TypeInformation[schema.size()];
        for (int i = 0; i < schema.size(); i++) {
            columnNames[i] = schema.get(i).getName();
            TypeInformation typeInformation = TypeStringUtils.readTypeInfo(schema.get(i).getType());
            if (typeInformation == null) {
                throw new UnsupportedOperationException("Unsupported type:" + schema.get(i).getType());
            }
            columnTypes[i] = typeInformation;
        }
        return (T) new CsvTableSource(path, columnNames, columnTypes, fieldDelim, rowDelim, quoteCharacter, ignoreFirstLine, ignoreComments, lenient);
    }

    @Override
    public String getType() {
        return Constants.CONNECTOR_TYPE_VALUE_CSV;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(path, "csv的path不能为空");
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFieldDelim() {
        return fieldDelim;
    }

    public void setFieldDelim(String fieldDelim) {
        this.fieldDelim = fieldDelim;
    }

    public String getRowDelim() {
        return rowDelim;
    }

    public void setRowDelim(String rowDelim) {
        this.rowDelim = rowDelim;
    }

    public Character getQuoteCharacter() {
        return quoteCharacter;
    }

    public void setQuoteCharacter(Character quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    public boolean isIgnoreFirstLine() {
        return ignoreFirstLine;
    }

    public void setIgnoreFirstLine(boolean ignoreFirstLine) {
        this.ignoreFirstLine = ignoreFirstLine;
    }

    public String getIgnoreComments() {
        return ignoreComments;
    }

    public void setIgnoreComments(String ignoreComments) {
        this.ignoreComments = ignoreComments;
    }

    public boolean isLenient() {
        return lenient;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }
}

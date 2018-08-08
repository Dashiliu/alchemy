package web.cluster;

import com.dfire.platform.alchemy.api.sink.HbaseInvoker;

/**
 * @author congbai
 * @date 07/06/2018
 */
public class TestHbaseInvoke implements HbaseInvoker {
    @Override
    public String getRowKey(Object[] rows) {
        return String.valueOf(rows[0]);
    }

    @Override
    public String getFamily(Object[] rows) {
        return "s";
    }

    @Override
    public String getQualifier(Object[] rows) {
        return String.valueOf(rows[0]);
    }
}

package web.descriptor;

import com.dfire.platform.alchemy.web.bind.BindPropertiesFactory;
import com.dfire.platform.alchemy.web.cluster.flink.SqlSubmitFlinkRequest;
import com.dfire.platform.alchemy.web.descriptor.ConnectorDescriptor;
import org.junit.Test;



/**
 * @author congbai
 * @date 2018/6/30
 */
public class BindPropertiesToTargetTest {

    @Test
    public void bind() throws Exception {
        String value="table:\n" +
            "    parallelism: 10\n" +
            "    checkpointingInterval: 5\n" +
            "    sql: select * from  TaxiRides\n" +
            "    sources:\n" +
            "      - name: TaxiRides\n" +
            "        schema:\n" +
            "          - name: rideId\n" +
            "            type: LONG\n" +
            "          - name: lon\n" +
            "            type: FLOAT\n" +
            "          - name: lat\n" +
            "            type: FLOAT\n" +
            "          - name: rowTime\n" +
            "            type: TIMESTAMP\n" +
            "            rowtime:\n" +
            "              timestamps:\n" +
            "                type: \"from-field\"\n" +
            "                from: \"rideTime\"\n" +
            "              watermarks:\n" +
            "                type: \"periodic-bounded\"\n" +
            "                delay: \"60000\"\n" +
            "        connector:\n" +
            "          property-version: 1\n" +
            "          type: kafka\n" +
            "          version: 0.11\n" +
            "          topic: TaxiRides\n" +
            "          startup-mode: earliest-offset\n" +
            "          properties:\n" +
            "            - key: zookeeper.connect\n" +
            "              value: localhost:2181\n" +
            "            - key: bootstrap.servers\n" +
            "              value: localhost:9092\n" +
            "            - key: group.id\n" +
            "              value: testGroup\n" +
            "        format:\n" +
            "          property-version: 1\n" +
            "          type: json\n" +
            "          schema: \"ROW(rideId LONG, lon FLOAT, lat FLOAT, rideTime TIMESTAMP)\"\n" +
            "\n";
        SqlSubmitFlinkRequest sqlSubmitFlinkRequest =new SqlSubmitFlinkRequest();
        BindPropertiesFactory.bindProperties(sqlSubmitFlinkRequest, "",value);
        ConnectorDescriptor connectorDescriptor=sqlSubmitFlinkRequest.getTable().getSources().get(0).getConnectorDescriptor();
        assert  connectorDescriptor != null ;
    }


}

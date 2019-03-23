package com.dfire.platform.alchemy.api.function.table;

import com.dfire.platform.alchemy.api.util.useragent.Client;
import com.dfire.platform.alchemy.api.util.useragent.UAModel;
import com.dfire.platform.alchemy.api.util.useragent.UserAgentUtil;
import org.apache.flink.table.functions.TableFunction;

/***
 * userAgent拆分函数
 * SELECT t1.user_agent, t2.* FROM Table1 AS t1, LATERAL TABLE(ua(t1.user_agent)) as t2
 */
public class UserAgentFunction extends TableFunction<UAModel> {

    public void eval(String value) {
        UAModel model = new UAModel();
        Client client = UserAgentUtil.parse(value);
        model.setUa_device(client.device.family);
        model.setUa_name(client.userAgent.family);
        model.setUa_major(client.userAgent.major);
        model.setUa_minor(client.userAgent.minor);
        model.setUa_patch(client.userAgent.patch);

        model.setUa_os(client.os.family);
        model.setUa_os_name(client.os.family);
        model.setUa_os_major(client.os.major);
        model.setUa_os_minor(client.os.minor);
        model.setUa_build("");
        collect(model);
    }

}

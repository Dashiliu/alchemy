package com.dfire.platform.alchemy.api.function.table;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import com.dfire.platform.alchemy.api.util.useragent.Client;
import com.dfire.platform.alchemy.api.util.useragent.UAModel;
import com.dfire.platform.alchemy.api.util.useragent.UserAgentUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.TableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * userAgent拆分函数
 * SELECT t1.user_agent, t2.* FROM Table1 AS t1, LATERAL TABLE(ua(t1.user_agent)) as t2
 */
public class UserAgentFunction extends TableFunction<UAModel> implements BaseFunction{

    private static final String FUNCTION_NANME = "USERAGENT";
    private static final Logger logger = LoggerFactory.getLogger(UserAgentFunction.class);

    @Override
    public String getFunctionName() {
        return FUNCTION_NANME;
    }

    public void eval(String value) {
        if (StringUtils.isBlank(value)){
            return;
        }
        try {
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
        }catch (Exception e){
            logger.error("user",e);
        }

    }

}

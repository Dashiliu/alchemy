package com.dfire.platform.alchemy.web.config.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.dfire.platform.alchemy.web.util.JsonUtils;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultMap;
import com.twodfire.share.result.ResultSupport;

public class ExceptionInterceptor extends SimpleMappingExceptionResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionInterceptor.class);

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object arg2,
        Exception ex) {
        if (arg2 == null) {
            return new ModelAndView();
        }
        HandlerMethod method = (HandlerMethod)arg2;
        Class<?> cl = method.getMethod().getReturnType();
        try {
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            Object result = null;
            String message = ex.getMessage();
            String code = "0";
            if (Result.class.equals(cl)) {
                result = new ResultSupport();
                ((ResultSupport)result).setMessage(message);
                ((ResultSupport)result).setSuccess(false);
                ((ResultSupport)result).setResultCode(String.valueOf(code));
            } else if (ResultMap.class.equals(cl)) {
                result = new ResultMap(String.valueOf(code), message);
            }
            if (result != null) {
                writer.write(JsonUtils.toJson(result));
                writer.flush();
            }
            LOGGER.error("exception", ex);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ModelAndView();
    }

}

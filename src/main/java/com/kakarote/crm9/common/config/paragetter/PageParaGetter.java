package com.kakarote.crm9.common.config.paragetter;

import com.alibaba.fastjson.util.TypeUtils;
import com.jfinal.core.Action;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.ParaGetter;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 分页参数拦截
 */
public class PageParaGetter extends ParaGetter<BasePageRequest> {
    public PageParaGetter(String parameterName, String defaultValue) {
        super(parameterName, defaultValue);
    }
    @Override
    protected BasePageRequest to(String s) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BasePageRequest get(Action action, Controller controller) {
        Parameter[] parameters=action.getMethod().getParameters();
        Class clazz=null;
        for (Parameter parameter:parameters){
            if(BasePageRequest.class.isAssignableFrom(parameter.getType())){
                Type parameterizedType=parameter.getParameterizedType();
                if (parameterizedType instanceof ParameterizedType) {
                    Type[] params = ((ParameterizedType) parameterizedType).getActualTypeArguments();
                    clazz= TypeUtils.getClass(params[0]);
                }
                break;
            }
        }
        boolean isJson=controller.getHeader("Content-Type")!=null&&controller.getHeader("Content-Type").toLowerCase().contains("application/json");
        return isJson?new BasePageRequest(controller.getRawData(),clazz):new BasePageRequest(controller.getKv(),clazz);
    }
}

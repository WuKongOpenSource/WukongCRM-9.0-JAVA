package com.kakarote.crm9.common.interceptor;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

public class ErpInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation invocation) {
        try {
            Controller controller = invocation.getController();
            BaseUtil.setRequest(controller.getRequest());
            String token = controller.getHeader("Admin-Token") != null ? controller.getHeader("Admin-Token") : controller.getCookie("Admin-Token", "");
            if (!Redis.use().exists(token)) {
                controller.renderJson(R.error(302, "请先登录！"));
                return;
            }
            NotNullValidate[] validates = invocation.getMethod().getAnnotationsByType(NotNullValidate.class);
            if (ArrayUtil.isNotEmpty(validates)) {
                if (HttpEnum.PARA.equals(validates[0].type())) {
                    for (NotNullValidate validate : validates) {
                        if (controller.getPara(validate.value()) == null) {
                            controller.renderJson(R.error(500, validate.message()));
                            return;
                        }
                    }
                } else if (HttpEnum.JSON.equals(validates[0].type())) {
                    JSONObject jsonObject = JSON.parseObject(controller.getRawData());
                    for (NotNullValidate validate : validates) {
                        if (!jsonObject.containsKey(validate.value())||jsonObject.get(validate.value())==null) {
                            controller.renderJson(R.error(500, validate.message()));
                            return;
                        }
                    }
                }
            }
            invocation.invoke();
        } catch (Exception e) {
            invocation.getController().renderJson(R.error("服务器响应异常"));
            Log.getLog(invocation.getController().getClass()).error("响应错误", e);
        } finally {
            BaseUtil.removeThreadLocal();
        }

    }
}

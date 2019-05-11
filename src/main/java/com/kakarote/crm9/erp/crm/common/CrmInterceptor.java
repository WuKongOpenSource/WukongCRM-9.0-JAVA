package com.kakarote.crm9.erp.crm.common;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

public class CrmInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation inv) {
        inv.invoke();
    }
}

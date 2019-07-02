package com.kakarote.crm9.erp.crm.common;

import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Scope;

public class CrmDirective extends Directive {
    @Override
    public void exec(Env env, Scope scope, Writer writer) {
        String as = exprList.eval(scope) != null ? exprList.eval(scope).toString() + "." : "";
        //TODO 暂时不需要sql注入式权限拦截

    }
}

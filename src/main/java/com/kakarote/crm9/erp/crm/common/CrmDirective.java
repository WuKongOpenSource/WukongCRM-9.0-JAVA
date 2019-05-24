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
        //List<Long> longs=new AdminUserService().queryUserByAuth(BaseUtil.getUserId());
        //writer.write(" and "+as+"owner_user_id in ("+ StrUtil.join(",",longs) +")");
        //writer.write(" or "+as+"ro_user_id like CONCAT('%,','"+BaseUtil.getUserId()+"',',%')" +" or "+as+"rw_user_id like CONCAT('%,','"+BaseUtil.getUserId()+"',',%')");

    }
}

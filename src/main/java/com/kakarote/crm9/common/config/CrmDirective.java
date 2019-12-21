package com.kakarote.crm9.common.config;

import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.template.Directive;
import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.expr.ast.Id;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Scope;

import java.util.Collection;
import java.util.Objects;

/**
 * Sql模板的for循环简易版
 * @author zhangzhiwei
 */
public class CrmDirective extends Directive {
    private String name;

    @Override
    public void exec(Env env, Scope scope, Writer writer) {
        Object obj = scope.get(name);
        if(Objects.isNull(obj)){
            obj=new String[0];
        }
        if (obj instanceof String) {
            write(scope, writer, (Object[]) ((String) obj).split(","));
        } else if (obj instanceof Collection) {
            write(scope, writer, ((Collection) obj).toArray());
        } else if (obj.getClass().isArray()) {
            write(scope, writer, (Object[]) obj);
        } else {
            write(scope, writer, obj);
        }
    }

    @Override
    public void setExprList(ExprList exprList) {
        if (exprList.getExprArray()[0] instanceof Id) {
            this.name = ((Id) exprList.getExprArray()[0]).getId();
        }
        super.setExprList(exprList);
    }

    private void write(Scope scope, Writer writer, Object... arrays) {
        SqlPara sqlPara = (SqlPara) scope.get("_SQL_PARA_");
        if(arrays.length==0){
            arrays=new Object[]{0};
        }
        for (int i = 0; i < arrays.length; i++) {
            if (i != 0) {
                write(writer, ",");
            }
            write(writer, "?");
            sqlPara.addPara(arrays[i]);
        }
    }

}

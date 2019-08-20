package com.kakarote.crm9.common.config.paragetter;

import com.jfinal.core.Action;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.ParaGetter;

import java.util.HashMap;
import java.util.Map;

public class MapParaGetter extends ParaGetter<Map> {

    public MapParaGetter(String parameterName, String defaultValue) {
        super(parameterName, defaultValue);
    }

    @Override
    protected Map<String,Object> to(String s) {
        return null;
    }

    @Override
    public Map<String,Object> get(Action action, Controller controller) {
        return new HashMap<>();
    }
}

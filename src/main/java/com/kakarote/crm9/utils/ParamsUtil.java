package com.kakarote.crm9.utils;

import java.util.regex.Pattern;

/**
 * @author wyq
 */
public class ParamsUtil {
    public boolean isValid(String param){
        String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
                + "(\\b(select|update|union|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";

        Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);

        if (sqlPattern.matcher(param).find()) {
            return false;
        }
        return true;
    }
}

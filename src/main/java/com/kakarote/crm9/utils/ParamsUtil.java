package com.kakarote.crm9.utils;

import java.util.regex.Pattern;

/**
 * @author wyq
 */
public class ParamsUtil {
    public static boolean isValid(String param){
        if(param==null){
            return true;
        }
        String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
                + "(\\b(select|update|union|and|or|delete|insert|trancate|char|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";

        Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);

        if (sqlPattern.matcher(param).find()) {
            return false;
        }
        return true;
    }
}

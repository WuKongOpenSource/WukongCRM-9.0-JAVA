package com.kakarote.crm9.erp.crm.common;

import cn.hutool.core.util.ArrayUtil;
import com.jfinal.plugin.activerecord.Db;

/**
 * @author wyq
 */
public class CrmParamValid {
    String[] wordArr = new String[]{"中国","公司","有限公司","有限责任公司","股份有限公司","集团","国际","分公司","总公司"};

    public boolean isValid(String para){
        Integer number = Db.queryInt("select count(*) from 72crm_crm_area where city_name = ?",para);
        if (ArrayUtil.contains(wordArr,para) || number > 0){
            return false;
        }
        return true;
    }
}

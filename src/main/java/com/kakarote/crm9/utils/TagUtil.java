package com.kakarote.crm9.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TagUtil{

    private static final String SEPARATOR =",";

    public static Set<Integer> toSet(String tagStr){
        Set<Integer> tag=new HashSet<>();
        if(StrUtil.isEmpty(tagStr)){
            return tag;
        }
        String[] split = tagStr.split(SEPARATOR);
        for (String s : split) {
            try {
                tag.add(Integer.valueOf(s));
            }catch (NumberFormatException ignore){}
        }
        return tag;
    }



    public static String fromSet(Collection<Integer> tag){
        if(CollectionUtil.isEmpty(tag)){
            return "";
        }
        StringBuilder sb=new StringBuilder(SEPARATOR);
        for (Integer integer : tag) {
            if(integer==null){
                continue;
            }
            sb.append(integer).append(SEPARATOR);
        }
        return sb.toString();
    }

    public static String fromString(String tagStr){
        if(StrUtil.isEmpty(tagStr)){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        if(!tagStr.substring(0,1).equals(SEPARATOR)){
            sb.append(SEPARATOR);
        }
        sb.append(tagStr);
        if(!tagStr.substring(tagStr.length()-1).equals(SEPARATOR)){
            sb.append(SEPARATOR);
        }
        return sb.toString();
    }

    public static void main(String[] args){
        String str = ",1,2,3,4,";
        System.out.println(fromString(str));
        Set<Integer> integers = toSet(str);
        System.out.println(integers.toString());
        str = null;
        System.out.println(fromString(str));
    }
}

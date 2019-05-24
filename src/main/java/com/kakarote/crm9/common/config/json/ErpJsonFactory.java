package com.kakarote.crm9.common.config.json;

import com.alibaba.fastjson.JSON;
import com.jfinal.json.IJsonFactory;
import com.jfinal.json.Json;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * IJsonFactory 的 fastjson 实现.
 */
public class ErpJsonFactory implements IJsonFactory {

    private static final ErpJsonFactory ME = new ErpJsonFactory();

    public static ErpJsonFactory me() {
        return ME;
    }

    @Override
    public Json getJson() {
        return new ErpJson();
    }

    protected class ErpJson extends Json {

        private int defaultConvertDepth = 16;

        private String timestampPattern = "yyyy-MM-dd HH:mm:ss";


        @Override
        public Json setDatePattern(String datePattern) {
            if (StrKit.isBlank(datePattern)) {
                throw new IllegalArgumentException("datePattern can not be blank.");
            }
            this.datePattern = datePattern;
            return this;
        }

        private String mapToJson(Map map, int depth) {
            if (map == null) {
                return "null";
            }
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            Iterator iter = map.entrySet().iterator();

            sb.append('{');
            while (iter.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                Map.Entry entry = (Map.Entry) iter.next();
                toKeyValue(String.valueOf(entry.getKey()), entry.getValue(), sb, depth);
            }
            sb.append('}');
            return sb.toString();
        }

        private void toKeyValue(String key, Object value, StringBuilder sb, int depth) {
            sb.append('\"');
            if (key == null) {
                sb.append("null");
            } else {
                escape(underlineToCame(key), sb);
            }
            sb.append('\"').append(':');
            sb.append(toJson(value, depth));
        }

        private String iteratorToJson(Iterator iter, int depth) {
            boolean first = true;
            StringBuilder sb = new StringBuilder();

            sb.append('[');
            while (iter.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                Object value = iter.next();
                if (value == null) {
                    sb.append("null");
                    continue;
                }
                sb.append(toJson(value, depth));
            }
            sb.append(']');
            return sb.toString();
        }

        private String escape(String s) {
            if (s == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            escape(s, sb);
            return sb.toString();
        }

        private void escape(String s, StringBuilder sb) {
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                switch (ch) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                            String str = Integer.toHexString(ch);
                            sb.append("\\u");
                            for (int k = 0; k < 4 - str.length(); k++) {
                                sb.append('0');
                            }
                            sb.append(str.toUpperCase());
                        } else {
                            sb.append(ch);
                        }
                }
            }
        }

        @Override
        public String toJson(Object object) {
            return toJson(object, this.defaultConvertDepth);
        }

        @Override
        public <T> T parse(String text, Class<T> clazz) {
            return JSON.parseObject(text, clazz);
        }

        @SuppressWarnings("unckecked")
        protected String toJson(Object value, int depth) {
            if (value == null || (depth--) < 0) {
                return "null";
            }
            if (value instanceof String) {
                return "\"" + escape((String) value) + "\"";
            } else if (value instanceof BigDecimal) {
                return "\"" + value.toString() + "\"";
            } else if (value instanceof Double) {
                if (((Double) value).isInfinite() || ((Double) value).isNaN()) {
                    return "null";
                } else {
                    return value.toString();
                }
            } else if (value instanceof Float) {
                if (((Float) value).isInfinite() || ((Float) value).isNaN()) {
                    return "null";
                } else {
                    return value.toString();
                }
            } else if (value instanceof Number) {
                return value.toString();
            } else if (value instanceof Boolean) {
                return value.toString();
            } else if (value instanceof java.util.Date) {
                if (value instanceof java.sql.Timestamp) {
                    return "\"" + new SimpleDateFormat(timestampPattern).format(value) + "\"";
                }
                if (value instanceof java.sql.Time) {
                    return "\"" + value.toString() + "\"";
                }
                String dp = datePattern != null ? datePattern : getDefaultDatePattern();
                if (dp != null) {
                    return "\"" + new SimpleDateFormat(dp).format(value) + "\"";
                } else {
                    return "" + ((java.util.Date) value).getTime();
                }
            } else if (value instanceof Collection) {
                return iteratorToJson(((Collection) value).iterator(), depth);
            } else if (value instanceof Map) {
                return mapToJson((Map) value, depth);
            }
            String result = otherToJson(value, depth);
            if (result != null) {
                return result;
            }
            return "\"" + escape(value.toString()) + "\"";
        }

        private String otherToJson(Object value, int depth) {
            if (value instanceof Character) {
                return "\"" + escape(value.toString()) + "\"";
            } else if (value instanceof Model) {
                Map map = com.jfinal.plugin.activerecord.CPI.getAttrs((Model) value);
                return mapToJson(map, depth);
            } else if (value instanceof Record) {
                Map map = ((Record) value).getColumns();
                return mapToJson(map, depth);
            } else if (value.getClass().isArray()) {
                int len = Array.getLength(value);
                List<Object> list = new ArrayList<Object>(len);
                for (int i = 0; i < len; i++) {
                    list.add(Array.get(value, i));
                }
                return iteratorToJson(list.iterator(), depth);
            } else if (value instanceof Iterator) {
                return iteratorToJson((Iterator) value, depth);
            } else if (value instanceof Enumeration) {
                ArrayList<?> list = Collections.list((Enumeration<?>) value);
                return iteratorToJson(list.iterator(), depth);
            } else if (value instanceof Enum) {
                return "\"" + ((Enum) value).toString() + "\"";
            }

            return beanToJson(value, depth);
        }

        private String beanToJson(Object model, int depth) {
            Map<String, Object> map = new HashMap<>();
            Method[] methods = model.getClass().getMethods();
            for (Method m : methods) {
                String methodName = m.getName();
                int indexOfGet = methodName.indexOf("get");
                if (indexOfGet == 0 && methodName.length() > 3) {
                    String attrName = methodName.substring(3);
                    if (!"Class".equals(attrName)) {
                        Class<?>[] types = m.getParameterTypes();
                        if (types.length == 0) {
                            try {
                                Object value = m.invoke(model);
                                map.put(StrKit.firstCharToLowerCase(attrName), value);
                            } catch (Exception e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        }
                    }
                } else {
                    int indexOfIs = methodName.indexOf("is");
                    if (indexOfIs == 0 && methodName.length() > 2) {
                        String attrName = methodName.substring(2);
                        Class<?>[] types = m.getParameterTypes();
                        if (types.length == 0) {
                            try {
                                Object value = m.invoke(model);
                                map.put(StrKit.firstCharToLowerCase(attrName), value);
                            } catch (Exception e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
            return mapToJson(map, depth);
        }

        private  String underlineToCame(String param) {
            if (param == null || "".equals(param.trim())) {
                return "";
            }
            int len = param.length();
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                char c = param.charAt(i);
                if (c == '_') {
                    if (i + 1 < len) {
                        if (param.charAt(i + 1) >= 0x0061 && param.charAt(i + 1) <= 0x007a) {
                            sb.append(Character.toUpperCase(param.charAt(++i)));
                        } else {
                            sb.append(c);
                        }
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}






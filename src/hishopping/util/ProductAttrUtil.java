package hishopping.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hishopping.entity.Product;

public class ProductAttrUtil {
    private static final int MAX_ATTRS = 20;
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{([^}]*)\\}");

    private ProductAttrUtil() {
    }

    public static String normalizeJson(String json) {
        return JsonUtil.toJson(attrMaps(json));
    }

    public static List<Map<String, Object>> attrMaps(Product product) {
        return attrMaps(product == null ? null : product.getProductAttrs());
    }

    public static List<Map<String, Object>> attrMaps(String json) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (json == null || json.trim().length() == 0) return list;
        Matcher matcher = OBJECT_PATTERN.matcher(json);
        while (matcher.find() && list.size() < MAX_ATTRS) {
            String body = matcher.group(1);
            String name = trim(field(body, "name"));
            String value = trim(field(body, "value"));
            if (name.length() == 0 || value.length() == 0) continue;
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("name", name);
            map.put("value", value);
            list.add(map);
        }
        return list;
    }

    private static String field(String body, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? unescape(matcher.group(1)) : "";
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String unescape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) {
                sb.append(c);
                continue;
            }
            char n = value.charAt(++i);
            if (n == '"' || n == '\\' || n == '/') sb.append(n);
            else if (n == 'b') sb.append('\b');
            else if (n == 'f') sb.append('\f');
            else if (n == 'n') sb.append('\n');
            else if (n == 'r') sb.append('\r');
            else if (n == 't') sb.append('\t');
            else if (n == 'u' && i + 4 < value.length()) {
                String hex = value.substring(i + 1, i + 5);
                try {
                    sb.append((char) Integer.parseInt(hex, 16));
                    i += 4;
                } catch (NumberFormatException e) {
                    sb.append("\\u").append(hex);
                    i += 4;
                }
            } else {
                sb.append(n);
            }
        }
        return sb.toString();
    }
}

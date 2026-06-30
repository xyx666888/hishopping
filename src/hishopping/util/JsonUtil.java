package hishopping.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public class JsonUtil {
    private JsonUtil() {
    }

    public static void write(HttpServletResponse response, Object value) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(toJson(value));
        out.flush();
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return quote((String) value);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            Iterator<?> it = ((Map<?, ?>) value).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
                sb.append(quote(String.valueOf(entry.getKey()))).append(":").append(toJson(entry.getValue()));
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            return sb.append("}").toString();
        }
        if (value instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            Iterator<?> it = ((Collection<?>) value).iterator();
            while (it.hasNext()) {
                sb.append(toJson(it.next()));
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            return sb.append("]").toString();
        }
        return quote(String.valueOf(value));
    }

    private static String quote(String text) {
        if (text == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append("\"").toString();
    }
}


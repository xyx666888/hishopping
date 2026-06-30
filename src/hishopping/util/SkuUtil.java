package hishopping.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hishopping.entity.Product;

public class SkuUtil {
    public static final String DEFAULT_SKU = "DEFAULT";

    private SkuUtil() {
    }

    public static class Attr {
        private String name;
        private List<String> values = new ArrayList<String>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getValues() { return values; }
        public void setValues(List<String> values) { this.values = values; }
    }

    public static class Sku {
        private String skuId;
        private List<String> values = new ArrayList<String>();
        private String text;
        private String color;
        private String spec;
        private double price;
        private double oldPrice;
        private int stock;
        private boolean enabled;
        private String skuCode;
        private String imageUrl;

        public String getSkuId() { return skuId; }
        public void setSkuId(String skuId) { this.skuId = skuId; }
        public List<String> getValues() { return values; }
        public void setValues(List<String> values) { this.values = values; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getSpec() { return spec; }
        public void setSpec(String spec) { this.spec = spec; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public double getOldPrice() { return oldPrice; }
        public void setOldPrice(double oldPrice) { this.oldPrice = oldPrice; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static List<Attr> attrs(Product product) {
        List<Attr> parsed = parseAttrs(product == null ? null : product.getSkuAttrs());
        if (!parsed.isEmpty()) return parsed;
        List<Sku> skus = parseSkus(product == null ? null : product.getSkuOptions());
        if (!skus.isEmpty()) return attrsFromSkus(skus);
        return legacyAttrs(product);
    }

    public static String normalizeAttrsJson(Product product, String rawAttrs, String rawSkus) {
        List<Attr> attrs = parseAttrs(rawAttrs);
        if (attrs.isEmpty()) {
            attrs = attrsFromSkus(parseSkus(rawSkus));
        }
        if (attrs.isEmpty()) {
            attrs = legacyAttrs(product);
        }
        return attrsToJson(attrs);
    }

    public static List<Sku> skus(Product product) {
        List<Sku> parsed = parseSkus(product == null ? null : product.getSkuOptions());
        if (!parsed.isEmpty()) return parsed;
        List<Sku> defaults = new ArrayList<Sku>();
        defaults.add(defaultSku(product));
        return defaults;
    }

    public static Sku defaultSku(Product product) {
        Sku sku = new Sku();
        List<Attr> attrs = legacyAttrs(product);
        List<String> values = new ArrayList<String>();
        for (Attr attr : attrs) {
            values.add(attr.getValues().isEmpty() ? "" : attr.getValues().get(0));
        }
        if (values.isEmpty()) {
            values.add("默认");
        }
        sku.setValues(values);
        sku.setSkuId(DEFAULT_SKU);
        sku.setColor(values.size() > 0 ? values.get(0) : "默认");
        sku.setSpec(values.size() > 1 ? values.get(1) : "标准");
        sku.setText(joinValues(values));
        sku.setPrice(product == null ? 0 : product.getPrice());
        sku.setOldPrice(product == null ? 0 : product.getOldPrice());
        sku.setStock(product == null ? 0 : product.getStock());
        sku.setEnabled(true);
        return sku;
    }

    public static Sku choose(Product product, String skuId, String color, String spec) {
        List<Sku> skus = skus(product);
        String wantedSku = empty(skuId) ? "" : skuId.trim();
        String wantedColor = empty(color) ? "" : color.trim();
        String wantedSpec = empty(spec) ? "" : spec.trim();
        for (Sku sku : skus) {
            if (!empty(wantedSku) && wantedSku.equals(sku.getSkuId())) return sku;
        }
        for (Sku sku : skus) {
            boolean colorOk = empty(wantedColor) || wantedColor.equals(sku.getColor());
            boolean specOk = empty(wantedSpec) || wantedSpec.equals(sku.getSpec());
            if (colorOk && specOk) return sku;
        }
        return skus.get(0);
    }

    public static Map<String, Object> attrMap(Attr attr) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("name", attr.getName());
        map.put("values", attr.getValues());
        return map;
    }

    public static List<Map<String, Object>> attrMaps(Product product) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Attr attr : attrs(product)) list.add(attrMap(attr));
        return list;
    }

    public static Map<String, Object> skuMap(Sku sku) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("skuId", sku.getSkuId());
        map.put("values", sku.getValues());
        map.put("text", skuText(sku));
        map.put("color", sku.getColor());
        map.put("spec", sku.getSpec());
        map.put("price", sku.getPrice());
        map.put("oldPrice", sku.getOldPrice());
        map.put("stock", sku.getStock());
        map.put("enabled", sku.isEnabled());
        map.put("skuCode", sku.getSkuCode());
        map.put("imageUrl", sku.getImageUrl());
        return map;
    }

    public static List<Map<String, Object>> skuMaps(Product product) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Sku sku : skus(product)) list.add(skuMap(sku));
        return list;
    }

    public static String skuText(Sku sku) {
        if (!empty(sku.getText())) return sku.getText();
        return joinValues(sku.getValues());
    }

    public static String normalizeJson(Product product, String raw) {
        List<Sku> skus = parseSkus(raw);
        if (skus.isEmpty()) skus.add(defaultSku(product));
        return toJson(skus);
    }

    public static String decrementStockJson(Product product, String skuId, int quantity) {
        List<Sku> skus = skus(product);
        boolean matched = false;
        for (Sku sku : skus) {
            if (sku.getSkuId().equals(skuId)) {
                matched = true;
                if (sku.getStock() < quantity) throw new RuntimeException("规格库存不足。");
                sku.setStock(sku.getStock() - quantity);
                break;
            }
        }
        if (!matched) throw new RuntimeException("商品规格不存在。");
        return toJson(skus);
    }

    public static String buildSkuId(String color, String spec) {
        String c = empty(color) ? "默认" : color.trim();
        String s = empty(spec) ? "标准" : spec.trim();
        return c + "__" + s;
    }

    private static List<Attr> parseAttrs(String raw) {
        List<Attr> list = new ArrayList<Attr>();
        if (empty(raw)) return list;
        Matcher matcher = Pattern.compile("\\{([^}]*)\\}").matcher(raw);
        while (matcher.find()) {
            String body = matcher.group(1);
            String name = text(body, "name");
            List<String> values = arrayText(body, "values");
            if (!empty(name) && !values.isEmpty()) {
                Attr attr = new Attr();
                attr.setName(name);
                attr.setValues(unique(values));
                list.add(attr);
            }
        }
        return list;
    }

    private static List<Sku> parseSkus(String raw) {
        List<Sku> list = new ArrayList<Sku>();
        if (empty(raw)) return list;
        Matcher objectMatcher = Pattern.compile("\\{([^}]*)\\}").matcher(raw);
        while (objectMatcher.find()) {
            String body = objectMatcher.group(1);
            Sku sku = new Sku();
            sku.setSkuId(firstNonEmpty(text(body, "skuId"), text(body, "key")));
            sku.setValues(arrayText(body, "values"));
            sku.setText(firstNonEmpty(text(body, "text"), text(body, "skuText")));
            sku.setColor(text(body, "color"));
            sku.setSpec(text(body, "spec"));
            if (sku.getValues().isEmpty()) {
                if (!empty(sku.getColor())) sku.getValues().add(sku.getColor());
                if (!empty(sku.getSpec())) sku.getValues().add(sku.getSpec());
            }
            if (empty(sku.getColor()) && !sku.getValues().isEmpty()) sku.setColor(sku.getValues().get(0));
            if (empty(sku.getSpec()) && sku.getValues().size() > 1) sku.setSpec(sku.getValues().get(1));
            if (empty(sku.getText())) sku.setText(joinValues(sku.getValues()));
            if (empty(sku.getSkuId())) sku.setSkuId(empty(sku.getText()) ? buildSkuId(sku.getColor(), sku.getSpec()) : sku.getText().replace(" / ", "|"));
            sku.setPrice(number(body, "price", 0));
            sku.setOldPrice(number(body, "oldPrice", sku.getPrice()));
            sku.setStock((int) number(body, "stock", 0));
            sku.setEnabled(bool(body, "enabled", true));
            sku.setSkuCode(text(body, "skuCode"));
            sku.setImageUrl(text(body, "imageUrl"));
            list.add(sku);
        }
        return list;
    }

    private static List<Attr> attrsFromSkus(List<Sku> skus) {
        List<Attr> attrs = new ArrayList<Attr>();
        int max = 0;
        for (Sku sku : skus) max = Math.max(max, sku.getValues().size());
        String[] names = { "参数1", "参数2", "参数3", "参数4" };
        for (int i = 0; i < max; i++) {
            LinkedHashSet<String> set = new LinkedHashSet<String>();
            for (Sku sku : skus) {
                if (sku.getValues().size() > i && !empty(sku.getValues().get(i))) set.add(sku.getValues().get(i));
            }
            if (!set.isEmpty()) {
                Attr attr = new Attr();
                attr.setName(i < names.length ? names[i] : ("参数" + (i + 1)));
                attr.setValues(new ArrayList<String>(set));
                attrs.add(attr);
            }
        }
        return attrs;
    }

    private static List<Attr> legacyAttrs(Product product) {
        List<Attr> attrs = new ArrayList<Attr>();
        List<String> colors = splitOptions(product == null ? null : product.getColorOptions());
        List<String> specs = splitOptions(product == null ? null : product.getSpecOptions());
        if (!colors.isEmpty()) {
            Attr color = new Attr();
            color.setName("颜色/款式");
            color.setValues(colors);
            attrs.add(color);
        }
        if (!specs.isEmpty()) {
            Attr spec = new Attr();
            spec.setName("规格");
            spec.setValues(specs);
            attrs.add(spec);
        }
        if (attrs.isEmpty()) {
            Attr attr = new Attr();
            attr.setName("规格");
            List<String> values = new ArrayList<String>();
            values.add("默认");
            attr.setValues(values);
            attrs.add(attr);
        }
        return attrs;
    }

    private static String attrsToJson(List<Attr> attrs) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        for (Attr attr : attrs) maps.add(attrMap(attr));
        return JsonUtil.toJson(maps);
    }

    private static String toJson(List<Sku> skus) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        for (Sku sku : skus) maps.add(skuMap(sku));
        return JsonUtil.toJson(maps);
    }

    private static List<String> splitOptions(String csv) {
        List<String> list = new ArrayList<String>();
        if (empty(csv)) return list;
        String[] parts = csv.split("[,，、\\n]");
        for (String part : parts) if (!empty(part)) list.add(part.trim());
        return unique(list);
    }

    private static List<String> unique(List<String> values) {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        for (String value : values) if (!empty(value)) set.add(value.trim());
        return new ArrayList<String>(set);
    }

    private static List<String> arrayText(String body, String key) {
        List<String> list = new ArrayList<String>();
        Matcher arrayMatcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[([^\\]]*)\\]").matcher(body);
        if (!arrayMatcher.find()) return list;
        Matcher itemMatcher = Pattern.compile("\"((?:\\\\.|[^\"])*)\"").matcher(arrayMatcher.group(1));
        while (itemMatcher.find()) list.add(unescape(itemMatcher.group(1)));
        return list;
    }

    private static String text(String body, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(body);
        return matcher.find() ? unescape(matcher.group(1)) : "";
    }

    private static double number(String body, String key, double fallback) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(body);
        if (!matcher.find()) return fallback;
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean bool(String body, String key, boolean fallback) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)").matcher(body);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : fallback;
    }

    private static String joinValues(List<String> values) {
        if (values == null || values.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (empty(value)) continue;
            if (sb.length() > 0) sb.append(" / ");
            sb.append(value.trim());
        }
        return sb.toString();
    }

    private static String firstNonEmpty(String a, String b) {
        return empty(a) ? b : a;
    }

    private static String unescape(String value) {
        return value == null ? "" : value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static boolean empty(String value) {
        return value == null || value.trim().length() == 0;
    }
}

package ru.nyansus.mc.fallenlink;

public final class JsonWriter {

    private final StringBuilder builder = new StringBuilder();
    private boolean needsComma;

    public void beginObject() {
        beforeValue();
        builder.append('{');
        needsComma = false;
    }

    public void endObject() {
        builder.append('}');
        needsComma = true;
    }

    public void name(String name) {
        beforeValue();
        builder.append(quote(name)).append(':');
        needsComma = false;
    }

    public void field(String name, String value) {
        name(name);
        builder.append(quote(value));
        needsComma = true;
    }

    public void field(String name, boolean value) {
        name(name);
        builder.append(value ? "true" : "false");
        needsComma = true;
    }

    public void field(String name, int value) {
        name(name);
        builder.append(value);
        needsComma = true;
    }

    public void field(String name, double value) {
        name(name);
        builder.append(value);
        needsComma = true;
    }

    public void rawField(String name, String json) {
        name(name);
        builder.append(json);
        needsComma = true;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    private void beforeValue() {
        if (needsComma) {
            builder.append(',');
        }
    }

    public static String quote(String value) {
        String safeValue = value == null ? "" : value;
        StringBuilder builder = new StringBuilder("\"");
        for (int i = 0; i < safeValue.length(); i++) {
            char c = safeValue.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    appendDefault(builder, c);
                    break;
            }
        }
        builder.append('"');
        return builder.toString();
    }

    private static void appendDefault(StringBuilder builder, char c) {
        if (c < 32) {
            builder.append(String.format("\\u%04x", (int) c));
            return;
        }
        builder.append(c);
    }
}

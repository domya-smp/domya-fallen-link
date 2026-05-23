package ru.nyansus.mc.fallenlink;

public final class JsonWriter {

    private JsonWriter() {
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

package com.example.taskapp.util;

import java.lang.reflect.RecordComponent;
import java.util.Map;

public final class Json {
    private Json() { }

    public static String of(Object o) {
        StringBuilder sb = new StringBuilder();
        write(sb, o);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Object o) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof Number || o instanceof Boolean) {
            sb.append(o);
        } else if (o instanceof Iterable<?> it) {
            sb.append('[');
            boolean first = true;
            for (Object x : it) {
                if (!first) sb.append(',');
                first = false;
                write(sb, x);
            }
            sb.append(']');
        } else if (o instanceof Map<?, ?> m) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                string(sb, String.valueOf(e.getKey()));
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (o.getClass().isRecord()) {
            sb.append('{');
            boolean first = true;
            for (RecordComponent rc : o.getClass().getRecordComponents()) {
                if (!first) sb.append(',');
                first = false;
                string(sb, rc.getName());
                sb.append(':');
                try {
                    write(sb, rc.getAccessor().invoke(o));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }
            sb.append('}');
        } else {
            string(sb, o.toString());
        }
    }

    private static void string(StringBuilder sb, String s) {
        sb.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }
}

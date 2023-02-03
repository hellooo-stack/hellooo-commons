package site.hellooo.commons.checks;

public final class ArgChecker extends BasicChecker {
    public static void check(boolean predicate) {
        if (!predicate) {
            throw new IllegalArgumentException();
        }
    }

    public static void check(boolean predicate, String message) {
        if (!predicate) {
            throw new IllegalArgumentException(stringValue(message));
        }
    }

    public static void check(boolean predicate, String message, Object... values) {
        if (!predicate) {
            throw new IllegalArgumentException(String.format(stringValue(message), values));
        }
    }

    public static <T> void checkNotNull(T ref, String message) {
        if (ref == null) {
            throw new NullPointerException(stringValue(message));
        }
    }
}

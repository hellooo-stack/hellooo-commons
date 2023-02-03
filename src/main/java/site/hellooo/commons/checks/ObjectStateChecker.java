package site.hellooo.commons.checks;

import java.util.Collection;
import java.util.Map;

public final class ObjectStateChecker extends BasicChecker {

    public static <K, V> void checkMapNotEmpty(Map<K, V> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new IllegalStateException(String.format(stringValue(message)));
        }
    }

    public static <T> void checkCollectionNotEmpty(Collection<T> elements, String message) {
        if (elements == null || elements.isEmpty()) {
            throw new IllegalStateException(String.format(stringValue(message)));
        }
    }
}

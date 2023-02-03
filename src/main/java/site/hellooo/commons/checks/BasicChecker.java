package site.hellooo.commons.checks;

import site.hellooo.commons.StringUtils;

public abstract class BasicChecker {
    protected static String stringValue(String message) {
        return message == null ? StringUtils.empty() : message;
    }
}

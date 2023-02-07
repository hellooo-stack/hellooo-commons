package site.hellooo.commons;

/**
 * @author Jeb.Wang
 */
public class StringUtils {

    public static final String EMPTY_STRING = "";

    public static String empty() {
        return EMPTY_STRING;
    }

    public static boolean isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence charSequence) {
        return !isEmpty(charSequence);
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }
}

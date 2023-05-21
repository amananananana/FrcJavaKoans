package engine;

import java.util.Arrays;
import java.util.Optional;
import static engine.Texts.*;

/**
 * Library of various assertions which can be run about the result of a koan execution.
 */
public class Assertions {
    private static String resolveParam(KoanResult res, Object p) {
        if (p instanceof StdInInput) {
            return res.inputLine((StdInInput)p);
        } else if (p instanceof FormatParam) {
            return ((FormatParam)p).format(res);
        }

        return Optional.ofNullable(p).map((v) -> v.toString()).orElse("");
    }

    private static String formatMethodCall(KoanResult res) {
        String[] params = Arrays.stream(res.koanParameters)
            .map(p -> p == null ? "null": p.toString())
            .toArray(String[]::new);
        return String.format("%s(%s)", res.koan.methodName, String.join(", ", params));
    }

    public static Assertion assertOutEquals(int outLineIndex, Localizable<String> expectedTemplate, Object... params) {

        return (locale, p, res) -> {
            final var realParams = Arrays.stream(params)
                .map((param) -> Assertions.resolveParam(res, param))
                .toArray();
            final var expected = String.format(expectedTemplate.get(locale), realParams);

            if (res.stdOutLines.length < outLineIndex + 1) {
                p.println(EXPECTED_TO_SEE_IN_CONSOLE_BUT_SAW_NOTHING, expected);
                return false;
            }
            if (!res.stdOutLines[outLineIndex].equals(expected)) {
                if (res.stdOutLines[outLineIndex].trim().equals("")) {
                    p.println(EXPECTED_TO_SEE_IN_CONSOLE_BUT_SAW_NOTHING, expected);
                } else {
                    p.println(EXPECTED_TO_SEE_IN_CONSOLE_BUT_SAW_INSTEAD, expected, res.stdOutLines[outLineIndex]);
                }
                return false;
            }

            p.println(OK_DISPLAYED_IN_CONSOLE, expected);
            return true;
        };
    }

    public static Assertion assertAskedInStdIn(final int inLineIndex) {
        return (locale, p, res) -> {
            if (res.inputLine(inLineIndex).isPresent()) {
                p.println(OK_ASKED_FOR_LINE_IN_CONSOLE);
                return true;
            }
            p.println(EXPECTED_FOR_USER_TO_ANSWER_IN_CONSOLE);
            return false;
        };
    }

    public static Assertion assertResultEquals(final int expected) {
        return (locale, p, res) -> {
            if (res.koanReturnValue == null) {
                p.println(EXPECTED_TO_RETURN_INT_BUT_RETURNED_NULL, formatMethodCall(res), expected);
                return false;
            } else if (!(res.koanReturnValue instanceof Integer)) {
                p.println(EXPECTED_TO_RETURN_INT_BUT_RETURNED_OTHER_TYPE, formatMethodCall(res), res.koanReturnValue.getClass().getSimpleName());
                return false;
            } else if (((Integer)res.koanReturnValue).intValue() != expected) {
                p.println(EXPECTED_TO_RETURN_INT_BUT_RETURNED, formatMethodCall(res), expected, ((Integer)res.koanReturnValue).intValue());
                return false;
            }

            p.println(OK_RETURNED_INT, formatMethodCall(res), expected);
            return true;
        }; 
    }

    public static Assertion assertResultEquals(final Localizable<String> expected) {
        return (locale, p, res) -> {
            if (res.koanReturnValue == null) {
                p.println(EXPECTED_TO_RETURN_STRING_BUT_RETURNED_NULL, formatMethodCall(res), expected);
                return false;
            } else if (!(res.koanReturnValue instanceof String)) {
                p.println(EXPECTED_TO_RETURN_STRING_BUT_RETURNED_OTHER_TYPE, formatMethodCall(res), res.koanReturnValue.getClass().getSimpleName());
                return false;
            } else if (!((String)res.koanReturnValue).equals(expected.get(locale))) {
                p.println(EXPECTED_TO_RETURN_STRING_BUT_RETURNED, formatMethodCall(res), expected, (String)res.koanReturnValue);
                return false;
            }

            p.println(OK_RETURNED_STRING, formatMethodCall(res), expected.get(locale));
            return true;
        }; 
    }
}

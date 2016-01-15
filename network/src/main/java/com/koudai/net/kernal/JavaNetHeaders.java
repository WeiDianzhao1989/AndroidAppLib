package com.koudai.net.kernal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class JavaNetHeaders {
    private JavaNetHeaders() {
    }

    private static final Comparator<String> FIELD_NAME_COMPARATOR = new Comparator<String>() {
        // @FindBugsSuppressWarnings("ES_COMPARING_PARAMETER_STRING_WITH_EQ")
        @Override public int compare(String a, String b) {
            if (a == b) {
                return 0;
            } else if (a == null) {
                return -1;
            } else if (b == null) {
                return 1;
            } else {
                return String.CASE_INSENSITIVE_ORDER.compare(a, b);
            }
        }
    };

    /**
     * Returns an immutable map containing each field to its list of values.
     *
     * @param valueForNullKey the request line for requests, or the status line for responses. If
     * non-null, this value is mapped to the null key.
     */
    public static Map<String, List<String>> toMultimap(Headers headers, String valueForNullKey) {
        Map<String, List<String>> result = new TreeMap<String, List<String>>(FIELD_NAME_COMPARATOR);
        for (int i = 0, size = headers.size(); i < size; i++) {
            String fieldName = headers.name(i);
            String value = headers.value(i);

            List<String> allValues = new ArrayList<String>();
            List<String> otherValues = result.get(fieldName);
            if (otherValues != null) {
                allValues.addAll(otherValues);
            }
            allValues.add(value);
            result.put(fieldName, Collections.unmodifiableList(allValues));
        }
        if (valueForNullKey != null) {
            result.put(null, Collections.unmodifiableList(Collections.singletonList(valueForNullKey)));
        }
        return Collections.unmodifiableMap(result);
    }
}


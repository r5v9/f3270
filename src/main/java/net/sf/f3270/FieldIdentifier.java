package net.sf.f3270;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.h3270.host.Field;

public class FieldIdentifier {

    private static final MatchMode DEFAULT_MATCH_MODE = MatchMode.CONTAINS;

    final String label;
    final int skip;
    final int matchNumber;
    final MatchMode matchMode;

    public FieldIdentifier(String label) {
        this(label, 1, 1, DEFAULT_MATCH_MODE);
    }

    public FieldIdentifier(String label, int skip) {
        this(label, skip, 1, DEFAULT_MATCH_MODE);
    }

    public FieldIdentifier(String label, MatchMode matchMode) {
        this(label, 1, matchMode);
    }

    public FieldIdentifier(String label, int skip, int matchNumber) {
        this(label, skip, matchNumber, DEFAULT_MATCH_MODE);
    }

    public FieldIdentifier(String label, int skip, MatchMode matchMode) {
        this(label, skip, 1, matchMode);
    }

    public FieldIdentifier(String label, int skip, int matchNumber, MatchMode matchMode) {
        this.label = label;
        this.skip = skip;
        this.matchNumber = matchNumber;
        this.matchMode = matchMode;
    }

    Collection<Parameter> buildParameters() {
        Collection<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("label", label));

        if (skip != 1) {
            parameters.add(new Parameter("skip", skip));
        }
        if (matchNumber != 1) {
            parameters.add(new Parameter("matchNumber", matchNumber));
        }
        if (matchMode != DEFAULT_MATCH_MODE) {
            parameters.add(new Parameter("matchMode", matchMode));
        }
        return parameters;
    }

    Field find(List<Field> fields) {
        int indexOfLabel = getFieldIndexOfLabel(fields);
        if (indexOfLabel == -1) {
            throw new RuntimeException(String.format("field [%s] could not be found using match mode [%s]", label,
                    matchMode));
        }
        final int indexOfField = indexOfLabel + skip;
        if (indexOfField >= fields.size()) {
            throw new RuntimeException(String.format("field [%s] at index [%i] plus skip [%i] exceed the number of available fields in the screen [%i]", label, indexOfLabel, skip, indexOfField));
        }
        return fields.get(indexOfField);

    }

    int getFieldIndexOfLabel(List<Field> fields) {
        int matches = 0;
        for (int i = 0; i < fields.size(); i++) {
            String value = fields.get(i).getValue().toLowerCase();
            if (matches(label.toLowerCase(), value)) {
                matches++;
                if (matches == matchNumber) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean matches(String expected, String actual) {
        return matchExact(expected, actual) || matchExactAfterTrim(expected, actual)
                || matchRegex(expected, actual) || matchContains(expected, actual);
    }

    private boolean matchExact(String expected, String actual) {
        return matchMode == MatchMode.EXACT && actual.equals(expected);
    }

    private boolean matchExactAfterTrim(String expected, String actual) {
        return matchMode == MatchMode.EXACT_AFTER_TRIM && actual.trim().equals(expected);
    }

    private boolean matchRegex(String expected, String actual) {
        return matchMode == MatchMode.REGEX && actual.matches(expected);
    }

    private boolean matchContains(String expected, String actual) {
        return matchMode == MatchMode.CONTAINS && actual.contains(expected);
    }
}

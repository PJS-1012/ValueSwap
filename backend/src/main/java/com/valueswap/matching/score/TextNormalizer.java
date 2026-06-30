package com.valueswap.matching.score;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class TextNormalizer {
    public String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    public boolean same(String left, String right) {
        String normalizedLeft = normalize(left);
        return !normalizedLeft.isEmpty() && normalizedLeft.equals(normalize(right));
    }

    public NameMatch compareNames(String provideName, String wantName) {
        String provide = normalize(provideName);
        String want = normalize(wantName);
        if (provide.isEmpty() || want.isEmpty()) {
            return new NameMatch(0, false);
        }
        if (provide.equals(want)) {
            return new NameMatch(30, true);
        }
        if (provide.contains(want) || want.contains(provide)) {
            return new NameMatch(20, true);
        }
        Set<String> provideTokens = tokens(provide);
        provideTokens.retainAll(tokens(want));
        return provideTokens.isEmpty() ? new NameMatch(0, false) : new NameMatch(10, true);
    }

    public int overlap(Collection<String> left, Collection<String> right) {
        Set<String> normalizedLeft = normalizedValues(left);
        normalizedLeft.retainAll(normalizedValues(right));
        return normalizedLeft.size();
    }

    private Set<String> tokens(String value) {
        Set<String> result = new HashSet<>();
        for (String token : value.split("[\\s\\p{P}\\p{S}]+")) {
            if (!token.isBlank()) {
                result.add(token);
            }
        }
        return result;
    }

    private Set<String> normalizedValues(Collection<String> values) {
        Set<String> result = new HashSet<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }
}

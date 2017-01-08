package com.github.grimsa.restviasoap;

import java.net.URI;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class QueryParameterExtractor {

    Map<String, String[]> toQueryParameters(URI requestUri) {
        String query = requestUri.getQuery();
        if (query == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> paramsAsLists = Arrays.stream(query.split("&")).map(this::parseParameter)
                .collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        Map<String, String[]> result = new LinkedHashMap<>();
        for (Entry<String, List<String>> param : paramsAsLists.entrySet()) {
            result.put(param.getKey(), param.getValue().stream().toArray(String[]::new));
        }
        return result;
    }

    private SimpleImmutableEntry<String, String> parseParameter(String it) {
        int idx = it.indexOf('=');
        String key = idx > 0 ? it.substring(0, idx) : it;
        String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new SimpleImmutableEntry<>(key, value);
    }
}

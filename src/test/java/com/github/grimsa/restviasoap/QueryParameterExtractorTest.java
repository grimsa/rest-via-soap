package com.github.grimsa.restviasoap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

public class QueryParameterExtractorTest {

    private QueryParameterExtractor extractor = new QueryParameterExtractor();

    @Test
    public void shouldReturnEmptyMapForNoQuery() {
        // given
        URI pathWithNoQuery = URI.create("/no-query;jsessionid=STILL_NOT_A_QUERY");

        // when
        Map<String, String[]> params = extractor.toQueryParameters(pathWithNoQuery);

        // then
        assertTrue(params.isEmpty());
    }

    @Test
    public void shouldReturnMultipleValues() {
        // given
        URI pathWithMultivalueQueryParams = URI.create("/url?param1=a&param2=b1&param2=b2");

        // when
        Map<String, String[]> params = extractor.toQueryParameters(pathWithMultivalueQueryParams);

        // then
        assertEquals(2, params.size());
        assertArrayEquals(new String[] { "a" }, params.get("param1"));
        assertArrayEquals(new String[] { "b1", "b2" }, params.get("param2"));
    }

    @Test
    public void shouldDecodeParameters() {
        // given
        URI pathWithEncodedParams = URI.create("/url?param_with_symbols+%2B%2F%3Fin_middle_of_name=a%C4%8Di%C5%AB");

        // when
        Map<String, String[]> params = extractor.toQueryParameters(pathWithEncodedParams);

        // then
        assertEquals(1, params.size());
        assertEquals(Collections.singleton("param_with_symbols +/?in_middle_of_name"), params.keySet());
        assertArrayEquals(new String[] { "ačiū" }, params.get("param_with_symbols +/?in_middle_of_name"));
    }
}

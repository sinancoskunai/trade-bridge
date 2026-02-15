package com.tradebridge.backend.parse.model;

import java.util.Map;

public record ParseResult(
        Map<String, String> parsedFields,
        Map<String, Double> confidence,
        boolean reviewRequired,
        String parserName) {
}

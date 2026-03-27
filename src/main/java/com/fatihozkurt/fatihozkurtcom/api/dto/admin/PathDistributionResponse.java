package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

/**
 * Represents path-level distribution metric.
 *
 * @param path path
 * @param count visit count
 */
public record PathDistributionResponse(
        String path,
        long count
) {
}

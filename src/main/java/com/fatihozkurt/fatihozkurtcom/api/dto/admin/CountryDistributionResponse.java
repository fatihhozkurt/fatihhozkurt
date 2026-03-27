package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

/**
 * Represents country-level visit distribution item.
 *
 * @param country country name
 * @param count visit count
 */
public record CountryDistributionResponse(
        String country,
        long count
) {
}

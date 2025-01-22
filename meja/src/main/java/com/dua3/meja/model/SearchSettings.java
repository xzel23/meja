package com.dua3.meja.model;

import java.util.Collection;
import java.util.List;

/**
 * A record representing the settings for a search operation.
 *
 * @param searchFromCurrent {@code true} to start the search from the current cell, otherwise from the first cell
 * @param ignoreCase  {@code true} to ignore the character case in search
 * @param matchComplete  {@code true} to only find cells where the full cell content matches, otherwise search for sub-string
 * @param updateCurrent {true} to set the current cell to the first match
 * @param searchFormula  {@code true} to search in formula strings
 */
public record SearchSettings(
        boolean searchFromCurrent,
        boolean ignoreCase,
        boolean matchComplete,
        boolean updateCurrent,
        boolean searchFormula
) {
    /**
     * Creates a new SearchSettings object based on the provided options.
     *
     * @param options a collection of SearchOptions to configure the search settings
     * @return a new SearchSettings object with the specified options
     */
    public static SearchSettings of(Collection<SearchOptions> options) {
        boolean searchFromCurrent = options.contains(SearchOptions.SEARCH_FROM_CURRENT);
        boolean ignoreCase = options.contains(SearchOptions.IGNORE_CASE);
        boolean matchComplete = options.contains(SearchOptions.MATCH_COMPLETE_TEXT);
        boolean updateCurrent = options.contains(SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND);
        boolean searchFormula = options.contains(SearchOptions.SEARCH_FORMULA_TEXT);

        return new SearchSettings(searchFromCurrent, ignoreCase, matchComplete, updateCurrent, searchFormula);
    }

    /**
     * Creates a new SearchSettings object based on the provided options.
     *
     * @param options an array of SearchOptions to configure the search settings
     * @return a new SearchSettings object with the specified options
     */
    public static SearchSettings of(SearchOptions... options) {
        return SearchSettings.of(List.of(options));
    }
}

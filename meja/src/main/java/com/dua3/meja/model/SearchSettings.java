package com.dua3.meja.model;

import java.util.Collection;
import java.util.List;

/**
 * Represents the settings for a search operation.
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

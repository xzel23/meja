package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchSettingsTest {

    @Test
    void testDefaults() {
        SearchSettings settings = SearchSettings.of();
        assertFalse(settings.searchFromCurrent());
        assertFalse(settings.ignoreCase());
        assertFalse(settings.matchComplete());
        assertFalse(settings.updateCurrent());
        assertFalse(settings.searchFormula());
    }

    @Test
    void testVarargsOf() {
        SearchSettings settings = SearchSettings.of(SearchOptions.IGNORE_CASE, SearchOptions.MATCH_COMPLETE_TEXT);
        assertFalse(settings.searchFromCurrent());
        assertTrue(settings.ignoreCase());
        assertTrue(settings.matchComplete());
        assertFalse(settings.updateCurrent());
        assertFalse(settings.searchFormula());
    }

    @Test
    void testCollectionOf() {
        SearchSettings settings = SearchSettings.of(List.of(SearchOptions.SEARCH_FORMULA_TEXT, SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND));
        assertFalse(settings.searchFromCurrent());
        assertFalse(settings.ignoreCase());
        assertFalse(settings.matchComplete());
        assertTrue(settings.updateCurrent());
        assertTrue(settings.searchFormula());
    }

    @Test
    void testSearchOptionsValues() {
        SearchOptions[] options = SearchOptions.values();
        assertEquals(5, options.length);
        assertEquals(SearchOptions.IGNORE_CASE, SearchOptions.valueOf("IGNORE_CASE"));
        assertEquals(SearchOptions.MATCH_COMPLETE_TEXT, SearchOptions.valueOf("MATCH_COMPLETE_TEXT"));
        assertEquals(SearchOptions.SEARCH_FORMULA_TEXT, SearchOptions.valueOf("SEARCH_FORMULA_TEXT"));
        assertEquals(SearchOptions.SEARCH_FROM_CURRENT, SearchOptions.valueOf("SEARCH_FROM_CURRENT"));
        assertEquals(SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND, SearchOptions.valueOf("UPDATE_CURRENT_CELL_WHEN_FOUND"));
    }
}

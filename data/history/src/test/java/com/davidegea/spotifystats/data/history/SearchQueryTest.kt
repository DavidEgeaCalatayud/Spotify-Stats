package com.davidegea.spotifystats.data.history

import org.junit.Assert.*
import org.junit.Test

class SearchQueryTest {
    @Test fun handlesQuotesAndFtsOperatorsAsLiteralTerms() {
        assertEquals("\"a\"* AND \"OR\"* AND \"b\"*", SearchQuery.compile("a\" OR (b) *"))
        assertNull(SearchQuery.compile("\"*() :"))
        assertEquals("\"Beyoncé\"* AND \"音楽\"*", SearchQuery.compile("Beyoncé 音楽"))
    }
}

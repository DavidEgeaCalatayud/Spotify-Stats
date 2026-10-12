package com.davidegea.spotifystats.ui.home

import org.junit.Assert.*
import org.junit.Test

class DemoDashboardTest {
    @Test fun previewIsUsableWithoutImport() {
        val preview = DemoDashboard.sample()
        assertFalse(preview.loading)
        assertNull(preview.error)
        assertEquals(30, preview.daily.size)
        assertTrue(preview.totalPlays > 0)
        assertEquals(preview.daily.sumOf { it.plays }, preview.totalPlays)
        assertNotNull(preview.topArtist)
        assertNotNull(preview.topAlbum)
        assertTrue(preview.recentActivity.isEmpty())
    }
}

package org.komputing.etherscan.downloader

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TheDownloader {

    @Test
    fun canParseJSON() {
        val json = javaClass.getResource("/content.json").readText()
        assertThat(filenameToContentToCodeAdapter.fromJson(json)?.keys?.size).isEqualTo(4)
    }
    @Test
    fun canParseAlternative() {
        val json = javaClass.getResource("/content_alternative.json").readText()
        assertThat(parseAlternativeJSON(json)?.keys?.size).isEqualTo(4)
    }

}
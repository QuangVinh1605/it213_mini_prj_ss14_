package com.example.mini_project_ss14.rag.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cho RagProperties — 9 cấu hình chunking theo SRS.
 */
class RagPropertiesTest {

    @ParameterizedTest
    @DisplayName("9 cấu hình chunking theo SRS")
    @CsvSource({
            "300, 0, 0",
            "300, 10, 30",
            "300, 20, 60",
            "500, 0, 0",
            "500, 10, 50",
            "500, 20, 100",
            "1000, 0, 0",
            "1000, 10, 100",
            "1000, 20, 200"
    })
    void shouldCalculateOverlapForAllConfigurations(int chunkSize, int overlapPercent, int expectedOverlap) {
        RagProperties props = new RagProperties();
        props.getChunk().setSize(chunkSize);
        props.getChunk().setOverlapPercent(overlapPercent);

        assertEquals(expectedOverlap, props.getOverlapSize(),
                String.format("ChunkSize=%d, Overlap=%d%% should give overlap=%d",
                        chunkSize, overlapPercent, expectedOverlap));
    }

    @Test
    @DisplayName("Giá trị mặc định hợp lý")
    void shouldHaveReasonableDefaults() {
        RagProperties props = new RagProperties();

        assertEquals(500, props.getChunk().getSize());
        assertEquals(10, props.getChunk().getOverlapPercent());
        assertEquals(0.7, props.getSimilarity().getThreshold());
        assertEquals(5, props.getSimilarity().getTopK());
    }
}

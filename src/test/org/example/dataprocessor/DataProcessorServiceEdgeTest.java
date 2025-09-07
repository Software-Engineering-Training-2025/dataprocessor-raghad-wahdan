package org.example.dataprocessor;

import org.example.dataprocessor.enums.AnalysisType;
import org.example.dataprocessor.enums.CleaningType;
import org.example.dataprocessor.enums.OutputType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessorServiceEdgeTest {

    @Test
    void emptyAfterCleaning_mean_nan() throws Exception {
        DataProcessorService svc = new DataProcessorService();
        double r = svc.process(
                CleaningType.REMOVE_NEGATIVES,
                AnalysisType.MEAN,
                OutputType.CONSOLE,
                List.of(-5, -1, -100)
        );
        assertTrue(Double.isNaN(r));
    }

    @Test
    void emptyAfterCleaning_stddev_nan() throws Exception {
        DataProcessorService svc = new DataProcessorService();
        double r = svc.process(
                CleaningType.REMOVE_NEGATIVES,
                AnalysisType.STD_DEV,
                OutputType.TEXT_FILE,
                List.of(-1, -2)
        );
        assertTrue(Double.isNaN(r));
    }

    @Test
    void empty_p90_nan() throws Exception {
        DataProcessorService svc = new DataProcessorService();
        double r = svc.process(
                CleaningType.REMOVE_NEGATIVES,
                AnalysisType.P90_NEAREST_RANK,
                OutputType.CONSOLE,
                List.of()
        );
        assertTrue(Double.isNaN(r));
    }

    @Test
    void empty_top3_returns_zero() throws Exception {
        DataProcessorService svc = new DataProcessorService();
        double r = svc.process(
                CleaningType.REMOVE_NEGATIVES,
                AnalysisType.TOP3_FREQUENT_COUNT_SUM,
                OutputType.CONSOLE,
                List.of()
        );
        assertEquals(0.0, r, 1e-9);
    }

    @Test
    void median_even_correct() throws Exception {
        DataProcessorService svc = new DataProcessorService();
        double r = svc.process(
                CleaningType.REPLACE_NEGATIVES_WITH_ZERO,
                AnalysisType.MEDIAN,
                OutputType.CONSOLE,
                List.of(10, 2, 8, 4) // cleaned -> [10,2,8,4] -> sorted [2,4,8,10] -> (4+8)/2 = 6.0
        );
        assertEquals(6.0, r, 1e-9);
    }
}

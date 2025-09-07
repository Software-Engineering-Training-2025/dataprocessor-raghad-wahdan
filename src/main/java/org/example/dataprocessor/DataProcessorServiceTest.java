package org.example.dataprocessor;

import static org.junit.jupiter.api.Assertions.*;
import org.example.dataprocessor.enums.AnalysisType;
import org.example.dataprocessor.enums.CleaningType;
import org.example.dataprocessor.enums.OutputType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class DataProcessorServiceTest {

    private DataProcessorService svc;

    @BeforeEach
    void setup() throws Exception {
        svc = new DataProcessorService();
        // Clean previous output (if any)
        Path p = Paths.get("target", "result.txt");
        if (Files.exists(p)) {
            Files.delete(p);
        }
    }

    @Test
    void mean_removeNegatives_console() throws Exception {
        double r = svc.process(
                CleaningType.REMOVE_NEGATIVES,
                AnalysisType.MEAN,
                OutputType.CONSOLE,
                List.of(5, -2, 7, 8)
        );
        // cleaned -> [5,7,8], mean = 20/3
        assertEquals(20.0 / 3.0, r, 1e-9);
    }

    @Test
    void median_replaceNegatives_textFile() throws Exception {
        double r = svc.process(
                CleaningType.REPLACE_NEGATIVES_WITH_ZERO,
                AnalysisType.MEDIAN,
                OutputType.TEXT_FILE,
                List.of(5, -2, 7, 8)
        );
        // cleaned -> [5,0,7,8] -> median = (5+7)/2 = 6.0
        assertEquals(6.0, r, 1e-9);

        Path p = Paths.get("target", "result.txt");
        assertTrue(Files.exists(p), "target/result.txt should be created");
        String content = Files.readString(p).trim();
        assertTrue(content.equals("Result = 6.0") || content.startsWith("Result = 6"),
                "File must contain 'Result = 6.0'");
    }

    @Test
    void stdDev_population_textFile() throws Exception {
        double r = svc.process(
                CleaningType.REPLACE_NEGATIVES_WITH_ZERO,
                AnalysisType.STD_DEV,
                OutputType.TEXT_FILE,
                List.of(2, 4, 4, 4, 5, 5, 7, 9)
        );
        // population std dev = 2.0
        assertEquals(2.0, r, 1e-9);
        assertTrue(Files.exists(Paths.get("target", "result.txt")));
    }

    @Test
    void p90_nearest_rank_console() throws Exception {
        double r = svc.process(
                CleaningType.REMOVE_NEGATIVES,
                AnalysisType.P90_NEAREST_RANK,
                OutputType.CONSOLE,
                List.of(1, 3, 5, 7, 9, 11, 13, 15, 17, 19)
        );
        // sorted rank ceil(0.9*10)=9 -> value = 17
        assertEquals(17.0, r, 1e-9);
    }

    @Test
    void top3_frequent_count_sum_console() throws Exception {
        double r = svc.process(
                CleaningType.REMOVE_NEGATIVES,
                AnalysisType.TOP3_FREQUENT_COUNT_SUM,
                OutputType.CONSOLE,
                List.of(5, 5, 5, 1, 1, 2, 2, 3)
        );
        // counts: 5->3, 1->2, 2->2, 3->1 => top3 counts = 3+2+2 = 7
        assertEquals(7.0, r, 1e-9);
    }
}

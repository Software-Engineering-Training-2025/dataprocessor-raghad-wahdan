package org.example.dataprocessor;

import org.example.dataprocessor.enums.AnalysisType;
import org.example.dataprocessor.enums.CleaningType;
import org.example.dataprocessor.enums.OutputType;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataProcessorService {

    public double process(
            CleaningType cleaningType,
            AnalysisType analysisType,
            OutputType outputType,
            List<Integer> data
    ) throws Exception {

        // 1) Defensive copy (null -> empty); never mutate caller's list
        List<Integer> input = (data == null) ? new ArrayList<>() : new ArrayList<>(data);

        // 2) Clean
        List<Integer> cleaned = new ArrayList<>();
        if (cleaningType == CleaningType.REMOVE_NEGATIVES) {
            for (int x : input) if (x >= 0) cleaned.add(x);
        } else { // REPLACE_NEGATIVES_WITH_ZERO
            for (int x : input) cleaned.add(x < 0 ? 0 : x);
        }

        // 3) Analyze (empty handling per spec)
        double result;
        if (cleaned.isEmpty()) {
            result = (analysisType == AnalysisType.TOP3_FREQUENT_COUNT_SUM) ? 0.0 : Double.NaN;
        } else {
            switch (analysisType) {
                case MEAN: {
                    double sum = 0.0;
                    for (int x : cleaned) sum += x;
                    result = sum / cleaned.size();
                    break;
                }
                case MEDIAN: {
                    List<Integer> s = new ArrayList<>(cleaned);
                    Collections.sort(s);
                    int n = s.size();
                    result = (n % 2 == 1)
                            ? s.get(n / 2)
                            : (s.get(n / 2 - 1) + s.get(n / 2)) / 2.0;
                    break;
                }
                case STD_DEV: { // population (divide by N)
                    double sum = 0.0; for (int x : cleaned) sum += x;
                    double mean = sum / cleaned.size();
                    double acc = 0.0;
                    for (int x : cleaned) { double d = x - mean; acc += d * d; }
                    result = Math.sqrt(acc / cleaned.size());
                    break;
                }
                case P90_NEAREST_RANK: {
                    List<Integer> s = new ArrayList<>(cleaned);
                    Collections.sort(s);
                    int n = s.size();
                    int rank = (int) Math.ceil(0.90 * n); // 1-based
                    if (rank < 1) rank = 1;
                    if (rank > n) rank = n;
                    result = s.get(rank - 1);
                    break;
                }
                case TOP3_FREQUENT_COUNT_SUM: {
                    // Count runs after sorting, then sum the top-3 counts
                    List<Integer> s = new ArrayList<>(cleaned);
                    Collections.sort(s);
                    List<Integer> counts = new ArrayList<>();
                    int count = 1;
                    for (int i = 1; i < s.size(); i++) {
                        if (s.get(i).equals(s.get(i - 1))) count++;
                        else { counts.add(count); count = 1; }
                    }
                    counts.add(count);
                    counts.sort(Collections.reverseOrder());
                    int k = Math.min(3, counts.size());
                    int sumTop = 0; for (int i = 0; i < k; i++) sumTop += counts.get(i);
                    result = sumTop;
                    break;
                }
                default:
                    result = Double.NaN;
            }
        }

        // 4) Output exactly "Result = <value>"
        String line = "Result = " + result;
        if (outputType == OutputType.CONSOLE) {
            System.out.println(line);
        } else { // TEXT_FILE
            Path out = Path.of("target", "result.txt");
            Files.createDirectories(out.getParent());
            Files.writeString(out, line, StandardCharsets.UTF_8);
        }
        // 5) Return
        return result;
    }
}

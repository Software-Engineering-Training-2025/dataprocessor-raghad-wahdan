package org.example.dataprocessor;

import org.example.dataprocessor.enums.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        DataProcessorService svc = new DataProcessorService();
        double r = svc.process(
                CleaningType.REMOVE_NEGATIVES,
                AnalysisType.MEAN,
                OutputType.CONSOLE,
                List.of(5, -2, 7, 8)
        );
        System.out.println("Returned = " + r);
    }
}

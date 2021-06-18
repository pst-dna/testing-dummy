package app.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class FibonacciBenchmark {

    @Test
    public void testFibonnacci() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(this.getClass().getSimpleName())
                .warmupIterations(1)
                .measurementIterations(1)
                .forks(1)
                .threads(1)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .build();
        new Runner(options).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void naivonacci(Blackhole blackhole, Parameters parameters) {
        blackhole.consume(naivonacci(parameters.getN()));
    }

    private int naivonacci(int n) {
        return n < 2 ? n : naivonacci(n - 1) + naivonacci(n - 2);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void optibonacci(Blackhole blackhole, Parameters parameters) {
        blackhole.consume(optibonacci(parameters.getN(), new HashMap<>()));
    }

    private int optibonacci(int n, Map<Integer, Integer> cache) {
        if (cache.containsKey(n))
            return cache.get(n);
        if (n < 2)
            return n;
        cache.put(n, optibonacci(n - 1, cache) + optibonacci(n - 2, cache));
        return optibonacci(n, cache);
    }

    @Data
    @NoArgsConstructor
    @State(Scope.Benchmark)
    public static class Parameters {
        @Param({"8", "16", "32"})
        private int n;
    }
}

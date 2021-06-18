package app.service;

import app.Application;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Profile("test")
@ExtendWith(SpringExtension.class)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MessageServiceBenchmark {

    private ConfigurableApplicationContext context;
    private MessageService messageService;

    @Test
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(String.format("\\.%s\\.", this.getClass().getSimpleName()))
                .warmupIterations(1)
                .measurementIterations(5)
                .forks(1)
                .threads(1)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .build();
        new Runner(options).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void benchmark(Blackhole blackhole, Parameters parameters) {
        blackhole.consume(messageService.get(parameters.getChannel()));
    }

    @Setup(Level.Iteration)
    public void init() {
        this.context = SpringApplication.run(Application.class);
        this.messageService = context.getBean(MessageService.class);
    }

    @TearDown(Level.Iteration)
    public void cleanup() {
        this.context.close();
    }

    @State(Scope.Benchmark)
    @Data
    @NoArgsConstructor
    public static class Parameters {
        @Param({"main", "default"})
        private String channel;
    }
}

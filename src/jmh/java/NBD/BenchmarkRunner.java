package NBD;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception { //Z konsoli ./gradlew jmh
//        Options options = new OptionsBuilder()
//                .include(CacheBenchmarkTest.class.getSimpleName())
//                .build();
//
//        new Runner(options).run();
//
//        Options options2 = new OptionsBuilder()
//                .include(NoCacheBenchmarkTest.class.getSimpleName())
//                .build();
//
//        new Runner(options2).run();

        Options options = new OptionsBuilder()
                .include(DatabaseApiBenchmark.class.getSimpleName())
                .build();

        new Runner(options).run();

        Options options2 = new OptionsBuilder()
                .include(RedisManagerBenchmark.class.getSimpleName())
                .build();

        new Runner(options2).run();

        // i potem testy cache managera
    }
}


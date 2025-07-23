package softcore.tictactoe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public final class ConcurrentTestHelper {

    private ConcurrentTestHelper() {}

    public static List<Throwable> runConcurrently(Runnable task, int threadCount) throws InterruptedException {
        return runConcurrently(task, threadCount, 2);
    }

    public static List<Throwable> runConcurrently(Runnable task, int threadCount, int timeoutSeconds) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = executor.invokeAll(
                Collections.nCopies(threadCount, Executors.callable(task, null))
        );
        executor.shutdown();
        executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);

        List<Throwable> exceptions = new ArrayList<>();
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                exceptions.add(e.getCause());
            }
        }

        return exceptions;
    }
}

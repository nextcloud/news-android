package de.luhmer.owncloudnewsreader.reader.owncloud;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompatFuture<T> implements Future<T> {
    private T result;

    public CompatFuture(T result) {
        this.result = result;
    }

    @Override
    public boolean cancel(boolean b) {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return result;
    }

    @Override
    public T get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return result;
    }
}

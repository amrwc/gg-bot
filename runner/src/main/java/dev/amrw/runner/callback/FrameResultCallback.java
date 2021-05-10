package dev.amrw.runner.callback;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import lombok.extern.log4j.Log4j2;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of {@link ResultCallback} with {@link Frame} type. Mainly used for displaying Docker logs. Based on
 * {@link com.github.dockerjava.api.async.ResultCallbackTemplate}.
 */
@Log4j2
public class FrameResultCallback implements ResultCallback<Frame> {

    private final CountDownLatch started = new CountDownLatch(1);
    private final CountDownLatch completed = new CountDownLatch(1);
    private Closeable stream;
    private boolean closed = false;
    private Throwable firstError = null;

    @Override
    public void onStart(final Closeable closeable) {
        this.stream = closeable;
        this.closed = false;
        started.countDown();
        log.debug("Started async Docker processing");
    }

    @Override
    public void onNext(final Frame object) {
        System.out.println(new String(object.getPayload()));
    }

    @Override
    public void onError(final Throwable throwable) {
        if (closed) {
            return;
        }

        if (this.firstError == null) {
            this.firstError = throwable;
        }

        try {
            log.error("Async Docker processing error", throwable);
        } finally {
            try {
                close();
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    public void onComplete() {
        try {
            close();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
        log.debug("Finished async Docker processing");
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            try {
                if (stream != null) {
                    stream.close();
                }
            } finally {
                completed.countDown();
            }
        }
        log.debug("Closed async Docker callback");
    }
}

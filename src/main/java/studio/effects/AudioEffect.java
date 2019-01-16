package studio.effects;

import lombok.Setter;

import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AudioEffect {

    protected SourceDataLine output;
    private Future<?> threadHandler;
    private ExecutorService executor;
    @Setter
    private byte[] audioBuffer;

    protected AudioEffect(SourceDataLine output) {
        this.output = output;
        this.executor = Executors.newSingleThreadExecutor();
    }


    public boolean isAlive() {
        return threadHandler != null && !threadHandler.isDone();
    }

    public void interrupt() {
        if (threadHandler != null) {
            threadHandler.cancel(true);
        }
    }

    public void start() {
        threadHandler = executor.submit(this::run);
    }

    protected abstract void run();

    public void cleanUp() {
        this.interrupt();
        this.interrupt();
        executor.shutdownNow();
    }
}

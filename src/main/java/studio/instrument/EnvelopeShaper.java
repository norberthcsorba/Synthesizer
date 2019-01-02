package studio.instrument;

import javafx.util.Callback;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import utils.Constants;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EnvelopeShaper {

    private SourceDataLine output;
    private FloatControl gainControl;
    private InstrumentString instrumentString;
    private ExecutorService executor;
    private Future<?> threadHandler;
    @Setter
    private static Consumer<Envelope> onEnvelopeChange;
    @Getter
    private static Envelope envelope;
    @Getter
    private boolean flag_Attack, flag_Release, flag_Mute;
    private static final float MAX_AMP = -1.0f;
    private static final float MIN_AMP = -40.0f;

    EnvelopeShaper(InstrumentString instrumentString, SourceDataLine output) {
        this.instrumentString = instrumentString;
        this.output = output;
        this.gainControl = (FloatControl) output.getControl(FloatControl.Type.MASTER_GAIN);
        this.executor = Executors.newSingleThreadExecutor();
    }

    static void setEnvelope(Envelope envelope){
        EnvelopeShaper.envelope = envelope;
        if(onEnvelopeChange != null){
            onEnvelopeChange.accept(envelope);
        }
    }

    boolean isAlive() {
        return threadHandler != null && !threadHandler.isDone();
    }

    void interrupt() {
        if (threadHandler != null) {
            threadHandler.cancel(true);
        }
        if (flag_Release) {
            flag_Mute = true;
        }
        if (flag_Attack) {
            flag_Release = true;
        }
        flag_Attack = true;
    }

    public void cleanUp() {
        executor.shutdownNow();
    }

    void start() {
        threadHandler = executor.submit(() -> {
            try {
                attack();
                System.out.println("attack finished");
                if (!envelope.hasDecayAndSustain) {
                    instrumentString.setFundamentalPitch(InstrumentString.NULL_PITCH);
                    throw new InterruptedException();
                }
                decay();
                System.out.println("decay finished");
                sustain();
            } catch (InterruptedException ex1) {
                try {
                    release();
                    System.out.println("release finished");
                    mute();
                    System.out.println("mute finished");
                } catch (InterruptedException ex2) {
                    mute();
                    System.out.println("mute finished");
                    flag_Attack = true;
                }
            }
        });
    }

    private void attack() throws InterruptedException {
        if (envelope.attackTime > 0) {
            fadeAmplitude(MIN_AMP, MAX_AMP, envelope.attackTime, this::isFlag_Release);
        } else {
            gainControl.setValue(MAX_AMP);
            checkInterruptFlag(this::isFlag_Release);
        }
    }

    private void decay() throws InterruptedException {
        if (envelope.decayTime > 0) {
            fadeAmplitude(gainControl.getValue(), envelope.sustainAmp, envelope.decayTime, this::isFlag_Release);
        } else {
            gainControl.setValue(envelope.sustainAmp);
            checkInterruptFlag(this::isFlag_Release);
        }
    }

    private void sustain() throws InterruptedException {
        synchronized (instrumentString.lock) {
            instrumentString.lock.wait();
        }
    }

    private void release() throws InterruptedException {
        if (envelope.releaseTime > 0) {
            fadeAmplitude(gainControl.getValue(), MIN_AMP, envelope.releaseTime, this::isFlag_Mute);
        }
    }

    private void mute() {
        synchronized (instrumentString.lock) {
            gainControl.setValue(MIN_AMP);
            output.flush();
            output.stop();
            output.close();
            try {
                output.open(output.getFormat(), Constants.SIZE_OF_TARGET_BUFFER);
                output.start();
            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
            }
            flag_Mute = flag_Release = flag_Attack = false;
        }
    }

    private void fadeAmplitude(float startAmp, float targetAmp, short availableTime, Supplier<Boolean> interruptFlag) throws InterruptedException {
        Predicate<Float> crtAmpHasNotReachedTarget = startAmp < targetAmp ? crtAmp -> crtAmp < targetAmp : crtAmp -> crtAmp > targetAmp;
        Function<Float, Float> incrementCrtAmp = startAmp < targetAmp ? crtAmp -> crtAmp + 1 : crtAmp -> crtAmp - 1;

        Map<Float, Float> f = new HashMap<>();
        for (float i = (int) startAmp; crtAmpHasNotReachedTarget.test(i); i = incrementCrtAmp.apply(i)) {
            f.put(i, -lin2log(-targetAmp, -startAmp, -i));
            checkInterruptFlag(interruptFlag);
        }
        f.put(targetAmp, -lin2log(-targetAmp, -startAmp, -targetAmp));

        final float linAmpStepPerMilis = (targetAmp - startAmp) / availableTime;
        final long startTime = System.currentTimeMillis();
        float linCrtAmp;
        do {
            Thread.sleep(10);
            final long dT = System.currentTimeMillis() - startTime;
            linCrtAmp = startAmp + dT * linAmpStepPerMilis;
            linCrtAmp = crtAmpHasNotReachedTarget.test(linCrtAmp) ? linCrtAmp : targetAmp;
            System.out.println(linCrtAmp);
            float logCrtAmp = f.get((float) Math.round(linCrtAmp));
            gainControl.setValue(logCrtAmp);
        } while (crtAmpHasNotReachedTarget.test(linCrtAmp));
    }


    private static float lin2log(float min, float max, float x) {
        double b = Math.log(max / min) / (max - min);
        double a = max / Math.exp(b * max);
        return (float) (a * Math.exp(b * x));
    }

    private void checkInterruptFlag(Supplier<Boolean> interruptFlag) throws InterruptedException {
        if (interruptFlag.get()) {
            throw new InterruptedException();
        }
    }


    @Getter
    @Setter
    @Builder
    public static class Envelope {
        private short attackTime = 50;
        private short decayTime = 50;
        private float sustainAmp = -1.0f;
        private short releaseTime = 100;
        private boolean hasDecayAndSustain = true;
    }

//    private int toIntAudioSample(byte[] audioBuffer, int pos) {
//        int sample = audioBuffer[pos] << 8;
//        sample += (audioBuffer[pos + 1] & 0x00ff);
//        return sample;
//    }
//
//    private void toByteAudioSample(byte[] audioBuffer, int pos, int sample) {
//        audioBuffer[pos] = (byte) ((sample >> 8) & 0xff);
//        audioBuffer[pos + 1] = (byte) (sample & 0xff);
//    }
}

package studio.instrument;

import lombok.Getter;
import lombok.Setter;
import studio.oscillators.WaveOscillator;
import utils.Constants;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;

public class EnvelopeShaper {

    private SourceDataLine output;
    private FloatControl gainControl;
    private WaveOscillator oscillator;
    private ExecutorService executor;
    private Future<?> threadHandler;
    private boolean flag_Attack, flag_Release, flag_Mute;
    @Getter
    @Setter
    private static short attackTime = 50;
    @Getter
    @Setter
    private static short decayTime = 50;
    @Getter
    @Setter
    private static float sustainAmp = -4.0f;
    @Getter
    @Setter
    private static boolean hasDecayAndSustain = true;
    @Getter
    @Setter
    private static short releaseTime = 100;

    private static final float MAX_AMP = -1.0f;
    private static final float MIN_AMP = -30.0f;

    public EnvelopeShaper(WaveOscillator oscillator, SourceDataLine output) {
        this.oscillator = oscillator;
        this.output = output;
        this.gainControl = (FloatControl) output.getControl(FloatControl.Type.MASTER_GAIN);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public boolean isAlive() {
        return threadHandler != null && !threadHandler.isDone();
    }

    public void interrupt() {
        if (threadHandler != null) {
            threadHandler.cancel(true);
            if (flag_Release) {
                flag_Mute = true;
            }
            if (flag_Attack) {
                flag_Release = true;
            }
        }
        flag_Attack = true;
    }

    public void cleanUp() {
        executor.shutdownNow();
    }

    public void start() {
        threadHandler = executor.submit(() -> {
            try {
                attack();
                System.out.println("attack finished");
                if (!hasDecayAndSustain) {
                    oscillator.setPitch(WaveOscillator.NULL_PITCH);
                    throw new InterruptedException();
                }
                decay();
                System.out.println("decay finished");
                sustain();
                System.out.println("sustain finished");
            } catch (InterruptedException ex1) {
                try {
                    release();
                    System.out.println("release finished");
                } catch (InterruptedException ex2) {

                } finally {
                    mute();
                    System.out.println("mute finished");
                }
            }
        });
    }

    private void attack() throws InterruptedException {
        fade(MIN_AMP, MAX_AMP, attackTime, flag_Release);
    }

    private void decay() throws InterruptedException {
//        fade(gainControl.getValue(), sustainAmp, decayTime, flag_Release);
    }

    private void sustain() throws InterruptedException {
        synchronized (this) {
            wait(); //wait until interrupted
        }
    }

    private void release() throws InterruptedException {
        fade(gainControl.getValue(), MIN_AMP, releaseTime, flag_Mute);
    }

    private void mute() {
        output.flush();
        output.stop();
        output.close();
        try {
            output.open();
            output.start();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
        gainControl.setValue(MIN_AMP);
        flag_Mute = flag_Release = flag_Attack = false;
    }

    private void fade(float startAmp, float targetAmp, short availableTime, boolean interruptFlag) throws InterruptedException {
        final float availableTimeInSamples = availableTime * Constants.nrOfSamplesPerMilis();
        final float linAmpStepPerSample = (targetAmp - startAmp) / availableTimeInSamples;

        Predicate<Float> crtAmpHasNotReachedTarget = startAmp < targetAmp ? crtAmp -> crtAmp < targetAmp : crtAmp -> crtAmp > targetAmp;
        Function<Float, Float> incrementCrtAmp = startAmp < targetAmp ? crtAmp -> crtAmp + 1 : crtAmp -> crtAmp - 1;

        Map<Float, Float> f = new HashMap<>();
        for (float i = (int) startAmp; crtAmpHasNotReachedTarget.test(i); i = incrementCrtAmp.apply(i)) {
            f.put(i, -lin2log(-targetAmp, -startAmp, -i));
        }

        long deltaSamples;
        long startFramePosition = output.getLongFramePosition();
        while ((deltaSamples = output.getLongFramePosition() - startFramePosition) < availableTimeInSamples) {
            float linCrtAmp = startAmp + deltaSamples * linAmpStepPerSample;
            linCrtAmp = crtAmpHasNotReachedTarget.test(linCrtAmp) ? linCrtAmp : targetAmp;
            float logCrtAmp = f.get((float) Math.round(linCrtAmp));
            gainControl.setValue(logCrtAmp);
            if (interruptFlag) {
                throw new InterruptedException();
            }
        }
    }


    private static float lin2log(float min, float max, float x) {
        double b = Math.log(max / min) / (max - min);
        double a = max / Math.exp(b * max);
        return (float) (a * Math.exp(b * x));
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

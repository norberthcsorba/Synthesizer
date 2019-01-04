package studio.effects;

import lombok.Getter;
import lombok.Setter;
import studio.utils.SampleConverter;

import javax.sound.sampled.SourceDataLine;
import java.util.function.Consumer;
import java.util.function.Function;

public class Distortion extends AudioEffect {

    @Setter
    private byte[] audioBuffer;
    @Setter
    private static boolean bypass = false;
    private static final float THRESHOLD = 0.1f;
    @Getter
    private static float preGain = 1.0f;
    @Getter
    private static float postGain = 10.0f;
    @Getter
    private static DistortionType distortionType = DistortionType.NORMAL;
    @Setter
    private static Consumer<Void> onPropertiesChange;

    public Distortion(SourceDataLine output) {
        super(output);
    }

    public static void setPreGain(float preGain) {
        Distortion.preGain = preGain;
        if (onPropertiesChange != null) {
            onPropertiesChange.accept(null);
        }
    }

    public static void setPostGain(float postGain) {
        Distortion.postGain = postGain;
        if (onPropertiesChange != null) {
            onPropertiesChange.accept(null);
        }
    }

    public static void setDistortionType(DistortionType distortionType) {
        Distortion.distortionType = distortionType;
        if (onPropertiesChange != null) {
            onPropertiesChange.accept(null);
        }
    }

    @Override
    protected void run() {
        if (bypass) {
            return;
        }
        for (int samplePos = 0; samplePos < audioBuffer.length; samplePos += 2) {
            float floatSample = SampleConverter.toFloatAudioSample(audioBuffer[samplePos], audioBuffer[samplePos + 1]);
            floatSample = distortionType.distortionStrategy.apply(floatSample);
            byte[] byteSample = SampleConverter.toByteAudioSample(floatSample);
            audioBuffer[samplePos] = byteSample[0];
            audioBuffer[samplePos + 1] = byteSample[1];
        }
    }

    @Getter
    public enum DistortionType {
        //preGain = 1.0, postGain = 1.0 => only subtle distort
        SOFT(sample -> {
            sample = (float) Math.atan(sample * preGain) * postGain;
            sample = sample > 1.0f ? 1.0f : sample;
            sample = sample < -1.0f ? -1.0f : sample;
            return sample;
        }),
        NORMAL(sample -> {
            sample = sample * preGain;
            sample = (sample < 0.0f) ? (float) (-1.0f + Math.exp(sample)) : (float) (1.0 - Math.exp(-sample));
            sample = sample * postGain;
            sample = sample > 1.0f ? 1.0f : sample;
            sample = sample < -1.0f ? -1.0f : sample;
            return sample;
        }),
        //preGain = 0.1, postGain = 10.0 => no distort
        HARD(sample -> {
            sample = sample * preGain;
            sample = sample > THRESHOLD ? THRESHOLD : sample;
            sample = sample < -THRESHOLD ? -THRESHOLD : sample;
            sample = sample * postGain;
            sample = sample > 1.0f ? 1.0f : sample;
            sample = sample < -1.0f ? -1.0f : sample;
            return sample;
        });

        private Function<Float, Float> distortionStrategy;

        DistortionType(Function<Float, Float> distortionStrategy) {
            this.distortionStrategy = distortionStrategy;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(this.name().toLowerCase());
            Character firstChar = stringBuilder.charAt(0);
            stringBuilder.replace(0, 1, firstChar.toString().toUpperCase());
            return stringBuilder.toString();
        }
    }

}

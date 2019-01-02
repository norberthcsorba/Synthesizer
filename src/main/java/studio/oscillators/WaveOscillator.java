package studio.oscillators;

import lombok.Getter;
import lombok.Setter;
import utils.Constants;

public abstract class WaveOscillator {

    @Getter
    @Setter
    private float amplitude;

    public float[] generateAudioBuffer(float pitch) {
        float[] buffer = new float[(int) (Constants.SAMPLE_RATE * Constants.SIZE_OF_BUFFER_IN_SECONDS)];
        for (int sampleNr = 0; sampleNr < buffer.length; sampleNr++) {
            float elapsedTime = (float) sampleNr / Constants.SAMPLE_RATE;
            buffer[sampleNr] = generateSample(pitch, elapsedTime);
        }
//        return trimAudioBufferToTwoCycles(buffer);
        return buffer;
    }

    private float[] trimAudioBufferToTwoCycles(float[] audioBuffer) {
        int i;
        int nrOFCycles = 0;
        for (i = 1; i < audioBuffer.length; i++) {
            if (audioBuffer[i] < 0.0f && audioBuffer[i + 1] >= 0.0f) {
                nrOFCycles++;
            }
            if (nrOFCycles == 40) {
                break;
            }
        }
        float[] trimmedBuffer = new float[i];
        for (i = 0; i < trimmedBuffer.length - 1; i++) {
            trimmedBuffer[i] = audioBuffer[i + 1];
        }
        return trimmedBuffer;
    }

//    public void cleanUp() {
//        envelopeShaper.cleanUp();
//    }

    protected abstract float generateSample(float pitch, float dT);

}

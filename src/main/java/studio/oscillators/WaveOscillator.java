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
        return buffer;
    }

    protected abstract float generateSample(float pitch, float dT);

}

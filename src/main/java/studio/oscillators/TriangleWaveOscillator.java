package studio.oscillators;

import javax.sound.sampled.AudioFormat;

public class TriangleWaveOscillator extends WaveOscillator {

    public TriangleWaveOscillator(AudioFormat audioFormat) {
        super(audioFormat);
    }

    @Override
    protected float generateSample(float dT) {
        return (float) Math.asin(Math.sin(getPitch() * 2 * Math.PI * dT));
    }
}

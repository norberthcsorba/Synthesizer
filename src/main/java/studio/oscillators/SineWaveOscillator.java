package studio.oscillators;

import javax.sound.sampled.AudioFormat;

public class SineWaveOscillator extends WaveOscillator {
    public SineWaveOscillator(AudioFormat audioFormat) {
        super(audioFormat);
    }

    @Override
    protected float generateSample(float dT) {
        return (float) Math.sin(super.getPitch() * 2 * Math.PI * dT);
    }
}

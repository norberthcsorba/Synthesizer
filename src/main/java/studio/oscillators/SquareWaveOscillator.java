package studio.oscillators;

import javax.sound.sampled.AudioFormat;

public class SquareWaveOscillator extends WaveOscillator {

    public SquareWaveOscillator(AudioFormat audioFormat){
        super(audioFormat);
    }

    @Override
    protected float generateSample(float dT) {
        return Math.sin(getPitch() * 2 * Math.PI * dT) > 0 ? 1 : -1;
    }
}

package studio.oscillators;

import javax.sound.sampled.AudioFormat;

public class TraingleWaveOscillator extends WaveOscillator {

    public TraingleWaveOscillator(AudioFormat audioFormat){
        super(audioFormat);
    }

    @Override
    protected float generateSample(float dT) {
        return (float) Math.asin(Math.sin(getPitch() * 2 * Math.PI * dT));
    }
}

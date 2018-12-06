package studio.oscillators;

import javax.sound.sampled.AudioFormat;

public class SawWaveOscillator extends WaveOscillator {

    public SawWaveOscillator(AudioFormat audioFormat){
        super(audioFormat);
    }

    @Override
    protected float generateSample(float dT) {
        float sample = 0;
        for(int n = 1; n <=50; n++){
            sample += -(Math.sin(n * getPitch() * 2 * Math.PI * dT))/n;
        }
        return sample;
    }
}

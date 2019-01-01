package studio.oscillators;

public class SawWaveOscillator extends WaveOscillator {

    @Override
    protected float generateSample(float pitch, float dT) {
        float sample = 0;
        for (int n = 1; n <= 20; n++) {
            sample += -(Math.sin(n * pitch * 2 * Math.PI * dT)) / n;
        }
        return sample * getAmplitude();
    }
}

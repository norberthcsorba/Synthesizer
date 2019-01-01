package studio.oscillators;

public class SquareWaveOscillator extends WaveOscillator {

    @Override
    protected float generateSample(float pitch, float dT) {
        return Math.sin(pitch * 2 * Math.PI * dT) > 0 ? 1 * getAmplitude() : -1 * getAmplitude();
    }
}

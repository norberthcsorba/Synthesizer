package studio.oscillators;

public class SineWaveOscillator extends WaveOscillator {

    @Override
    protected float generateSample(float pitch, float dT) {
        return (float) Math.sin(pitch * 2 * Math.PI * dT) * getAmplitude();
    }
}

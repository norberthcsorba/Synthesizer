package studio.oscillators;

public class TriangleWaveOscillator extends WaveOscillator {

    @Override
    protected float generateSample(float pitch, float dT) {
        return (float) Math.asin(Math.sin(pitch * 2 * Math.PI * dT)) * getAmplitude();
    }
}

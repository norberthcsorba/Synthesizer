package studio.instrument;

import lombok.Getter;
import studio.effects.AudioEffect;
import studio.oscillators.WaveOscillator;
import studio.utils.SampleConverter;
import utils.Constants;

import javax.sound.sampled.SourceDataLine;
import java.util.List;

public class InstrumentString extends Thread {

    public static final float NULL_PITCH = 0.0f;
    @Getter
    private final Object lock;
    private SourceDataLine output;
    private List<AudioEffect> effects;
    private List<WaveOscillator> oscillators;
    @Getter
    private boolean busy;
    @Getter
    private float fundamentalPitch;
    private byte harmonic;

    InstrumentString(SourceDataLine output, List<WaveOscillator> oscillators, List<AudioEffect> effects) {
        this.output = output;
        this.lock = new Object();
        this.effects = effects;
        this.oscillators = oscillators;
        this.fundamentalPitch = NULL_PITCH;
        this.busy = false;
    }

    public void setFundamentalPitch(float fundamentalPitch) {
        synchronized (lock) {
            effects.forEach(AudioEffect::interrupt);
            if (fundamentalPitch == NULL_PITCH) {
                busy = false;
            } else {
                busy = true;
                lock.notify();
            }
            this.fundamentalPitch = fundamentalPitch;
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (lock) {
                    if (fundamentalPitch == NULL_PITCH) {
                        lock.wait();
                    }
                }
                byte[] audioBuffer = generateAudioBuffer();
                synchronized (lock) {
                    effects.forEach(effect -> {
                        if (!effect.isAlive()) {
                            effect.setAudioBuffer(audioBuffer);
                            effect.start();
                        }
                    });
                    Thread.sleep(10);
                }
                output.write(audioBuffer, 0, audioBuffer.length);
            }
        } catch (InterruptedException ex) {

        }
    }

    private byte[] generateAudioBuffer() {
        this.harmonic = 1;
        float[] floatAudioBuffer = oscillators.stream()
                .map(osc -> osc.generateAudioBuffer(this.fundamentalPitch * this.harmonic++))
                .reduce((buffer1, buffer2) -> {
                    for (int sampleNr = 0; sampleNr < buffer1.length && sampleNr < buffer2.length; sampleNr++) {
                        buffer1[sampleNr] = (buffer1[sampleNr] + buffer2[sampleNr]) / 2;
                    }
                    return buffer1;
                }).get();
        return convertToByteAudioBuffer(floatAudioBuffer);
    }


    private byte[] convertToByteAudioBuffer(float[] floatBuffer) {
        int byteDepth = Constants.BIT_DEPTH / 8;
        byte[] byteBuffer = new byte[floatBuffer.length * byteDepth];
        for (int i = 0, j = 0; i < floatBuffer.length; i++, j += 2) {
            byte[] byteSample = SampleConverter.toByteAudioSample(floatBuffer[i]);
            byteBuffer[j] = byteSample[0];
            byteBuffer[j + 1] = byteSample[1];
        }
        return byteBuffer;
    }


    void cleanUp() {
        this.interrupt();
        effects.forEach(AudioEffect::cleanUp);
    }
}

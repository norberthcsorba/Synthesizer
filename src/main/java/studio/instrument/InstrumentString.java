package studio.instrument;

import lombok.Getter;
import lombok.Setter;
import studio.oscillators.WaveOscillator;
import utils.Constants;

import javax.sound.sampled.SourceDataLine;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class InstrumentString extends Thread {

    private SourceDataLine output;
    private ReentrantLock lock;
    private Condition cond_pitchIsNonNull;
    private EnvelopeShaper envelopeShaper;
    List<WaveOscillator> oscillators;
    public static final float NULL_PITCH = 0.0f;
    @Getter
    private boolean busy;
    @Getter
    @Setter
    private float fundamentalPitch;
    private byte harmonic;

    InstrumentString(SourceDataLine output, List<WaveOscillator> oscillators) {
        this.output = output;
        this.lock = new ReentrantLock();
        this.cond_pitchIsNonNull = lock.newCondition();
        this.envelopeShaper = new EnvelopeShaper(this, output);
        this.oscillators = oscillators;
        this.fundamentalPitch = NULL_PITCH;
        this.busy = false;
    }

    void setFundamentalPitch(float fundamentalPitch) {
        envelopeShaper.interrupt();
        if (fundamentalPitch == NULL_PITCH) {
            busy = false;
        } else {
            busy = true;
            lock.lock();
            cond_pitchIsNonNull.signal();
            lock.unlock();
        }
        this.fundamentalPitch = fundamentalPitch;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (fundamentalPitch == NULL_PITCH) {
                    lock.lock();
                    cond_pitchIsNonNull.await();
                    lock.unlock();
                }
                byte[] audioBuffer = generateAudioBuffer();
                if (!envelopeShaper.isAlive()) {
                    envelopeShaper.start();
                }
                output.write(audioBuffer, 0, audioBuffer.length);
            }
        } catch (InterruptedException ex) {

        }
    }

    private byte[] generateAudioBuffer() {
        this.harmonic = 1;
        float[] floatAudioBuffer = oscillators.parallelStream()
                .map(osc -> osc.generateAudioBuffer(this.fundamentalPitch * this.harmonic++))
                .reduce((buffer1, buffer2) -> {
                    for (int sampleNr = 0; sampleNr < buffer1.length && sampleNr < buffer2.length; sampleNr++) {
                        buffer1[sampleNr] = (buffer1[sampleNr] + buffer2[sampleNr]) / 2;
//                        buffer1[sampleNr] = (float)Math.sin(buffer1[sampleNr] + buffer2[sampleNr]);
                    }
                    return buffer1;
                }).get();
        return convertToByteAudioBuffer(floatAudioBuffer);
    }

    private float denormalizeSample(float sample) {
        return (float) (Math.pow(2, Constants.BIT_DEPTH - 1) - 1) * sample;
    }

    private byte[] convertToByteAudioBuffer(float[] floatBuffer) {
        int byteDepth = Constants.BIT_DEPTH / 8;
        byte[] byteBuffer = new byte[floatBuffer.length * byteDepth];
        for (int i = 0, j = 0; i < floatBuffer.length; i++, j += 2) {
            int sample = (int) denormalizeSample(floatBuffer[i]);
            byteBuffer[j] = (byte) ((sample >> 8) & 0xff);
            byteBuffer[j + 1] = (byte) (sample & 0xff);
        }
        return byteBuffer;
    }


}

package studio.oscillators;

import lombok.Getter;
import lombok.Setter;
import studio.instrument.EnvelopeShaper;
import utils.Constants;

import javax.sound.sampled.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class WaveOscillator extends Thread {

    private AudioFormat audioFormat;
    private SourceDataLine output;
    private ReentrantLock lock;
    private Condition cond_pitchIsNonNull;
    private EnvelopeShaper envelopeShaper;
    public static final float NULL_PITCH = 0.0f;
    @Getter
    private boolean busy;
    @Getter
    @Setter
    private float pitch;


    WaveOscillator(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        this.pitch = NULL_PITCH;
        lock = new ReentrantLock();
        cond_pitchIsNonNull = lock.newCondition();

        try {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            this.output = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            output.open(audioFormat);
            output.start();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }

        envelopeShaper = new EnvelopeShaper(this, output);
    }

    public void setPitch(float pitch) {
        envelopeShaper.interrupt();
        if (pitch == NULL_PITCH) {
            busy = false;
        } else {
            busy = true;
            lock.lock();
            cond_pitchIsNonNull.signal();
            lock.unlock();
        }
        this.pitch = pitch;
    }

    private byte[] generateSampleBuffer() {
        float[] buffer = new float[Constants.SAMPLE_RATE * Constants.SIZE_OF_BUFFER_IN_SECONDS];
        for (int sampleNr = 0; sampleNr < buffer.length; sampleNr++) {
            float elapsedTime = (float) sampleNr / Constants.SAMPLE_RATE;
            buffer[sampleNr] = generateSample(elapsedTime);
        }
        return convertToByteAudioBuffer(buffer);
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

    @Override
    public void run() {
        try {
            while (true) {
                if (pitch == NULL_PITCH) {
                    lock.lock();
                    cond_pitchIsNonNull.await();
                    lock.unlock();
                }
                byte[] audioBuffer = generateSampleBuffer();
                if (!envelopeShaper.isAlive()) {
                    envelopeShaper.start();
                }
                output.write(audioBuffer, 0, audioBuffer.length);
            }
        } catch (InterruptedException ex) {

        }
    }

    public void cleanUp(){
        envelopeShaper.cleanUp();
    }

    protected abstract float generateSample(float dT);

}

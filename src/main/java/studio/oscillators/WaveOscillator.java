package studio.oscillators;

import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class WaveOscillator extends Thread {

    private AudioFormat audioFormat;
    private SourceDataLine output;
    private ReentrantLock lock;
    private Condition cond_pitchIsNonNull;
    @Getter
    @Setter
    private boolean busy;

    @Getter
    @Setter
    private float pitch;


    public WaveOscillator(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        this.pitch = 0.0f;
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
    }

    public void setPitch(float pitch) {
        if (this.pitch != pitch) {
            output.flush();
        }
        if (pitch != 0.0f) {
            lock.lock();
            cond_pitchIsNonNull.signal();
            lock.unlock();
        }
        this.pitch = pitch;
    }

    private byte[] generateSampleBuffer() {
        int sampleRate = (int) audioFormat.getSampleRate();
        float[] buffer = new float[sampleRate];
        for (int sampleNr = 0; sampleNr < buffer.length; sampleNr++) {
            float elapsedTime = (float) sampleNr / sampleRate;
            buffer[sampleNr] = generateSample(elapsedTime);
        }
        return convertToByteAudioBuffer(buffer);
    }

    private float denormalizeSample(float sample) {
        return (float) (Math.pow(2, audioFormat.getSampleSizeInBits() - 1) - 1) * sample;
    }

    private byte[] convertToByteAudioBuffer(float[] floatBuffer) {
        int sampleRate = (int) audioFormat.getSampleRate();
        int byteDepth = audioFormat.getSampleSizeInBits() / 8;
        byte[] byteBuffer = new byte[sampleRate * byteDepth];
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
                if (pitch == 0.0f) {
                    lock.lock();
                    cond_pitchIsNonNull.await();
                    lock.unlock();
                }
                byte[] audioBuffer = generateSampleBuffer();
                output.write(audioBuffer, 0, (int) audioFormat.getSampleRate());
            }
        } catch (InterruptedException ex) {

        }
    }

    protected abstract float generateSample(float dT);
}

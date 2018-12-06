package studio.oscillators;

import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;

public abstract class WaveOscillator extends Thread {

    private AudioFormat audioFormat;
    private SourceDataLine output;
    @Getter @Setter
    private boolean busy;

    @Getter @Setter
    private float pitch;


    public WaveOscillator(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        this.pitch = 0.0f;
        try{
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            this.output = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            output.open(audioFormat);
            output.start();
        }catch (LineUnavailableException ex){
            ex.printStackTrace();
        }
    }

    public void setPitch(float pitch){
        if(this.pitch != pitch){
            output.flush();
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

    private float denormalizeSample(float sample){
        return (float) (Math.pow(2, audioFormat.getSampleSizeInBits()-1)-1) * sample;
    }

    private byte[] convertToByteAudioBuffer(float[] floatBuffer){
        int sampleRate = (int) audioFormat.getSampleRate();
        int byteDepth = audioFormat.getSampleSizeInBits()/8;
        byte[] byteBuffer = new byte[sampleRate * byteDepth];
        for(int i=0,j=0; i < floatBuffer.length; i++, j+=2){
            int sample = (int) denormalizeSample(floatBuffer[i]);
            byteBuffer[j] = (byte)((sample >> 8) & 0xff);
            byteBuffer[j+1] = (byte)(sample & 0xff);
        }

        return byteBuffer;
    }

    @Override
    public void run() {
        try{
            while(true){
                while(pitch == 0.0f){
                    Thread.sleep(50);
                }
                byte[] audioBuffer = generateSampleBuffer();
                output.write(audioBuffer, 0, (int)audioFormat.getSampleRate());
            }
        }catch (InterruptedException ex){

        }
    }

    protected abstract float generateSample(float dT);
}

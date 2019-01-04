package studio.utils;

import utils.Constants;

public final class SampleConverter {
    public static float denormalizeSample(float sample) {
        return (float) (Math.pow(2, Constants.BIT_DEPTH - 1) - 1) * sample;
    }

    public static float normalizeSample(float sample) {
        return (float) (sample / (Math.pow(2, Constants.BIT_DEPTH - 1) - 1));
    }

    public static float toFloatAudioSample(byte firstHalfOfSample, byte secondHalfOfSample) {
        int intSample = (firstHalfOfSample) << 8;
        intSample += (secondHalfOfSample & 0xff);
        return normalizeSample((float) intSample);
    }

    public static byte[] toByteAudioSample(float sample) {
        int intSample = (int) denormalizeSample(sample);
        byte[] byteSample = new byte[2];
        byteSample[0] = (byte) ((intSample >> 8) & 0xff);
        byteSample[1] = (byte) (intSample & 0xff);
        return byteSample;
    }
}

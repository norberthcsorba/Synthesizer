package utils;

public final class Constants {
    public static final String PIANO_KEYBOARD_MAPPING_FILE_PATH = "src/main/resources/piano-keyboard-mapping.xml";
    public static final String INSTRUMENTS_FOLDER_PATH = "src/main/resources/instruments";
    public static final int SAMPLE_RATE = 44100;
    public static final int BIT_DEPTH = 16;
    public static final int NR_OF_CHANNELS = 1;
    public static final boolean AUDIO_FORMAT_IS_SIGNED = true;
    public static final boolean AUDIO_FORMAT_IS_BIG_ENDIAN = true;
    public static final int SIZE_OF_BUFFER_IN_SECONDS = 3;

    public static float nrOfSamplesPerMilis() {
        return (float) Constants.SAMPLE_RATE / 1000;
    }
}

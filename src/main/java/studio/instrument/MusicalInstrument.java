package studio.instrument;

public interface MusicalInstrument {

    boolean startPlaying(float pitch);

    boolean stopPlaying(float pitch);

    void setBypass(boolean bypass);

    void cleanUp();
}

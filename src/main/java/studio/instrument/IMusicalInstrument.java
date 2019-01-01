package studio.instrument;

public interface IMusicalInstrument {

    boolean play(float pitch);

    boolean stop(float pitch);

    void cleanUp();
}

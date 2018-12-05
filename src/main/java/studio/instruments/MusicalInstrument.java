package studio.instruments;

import lombok.Getter;
import studio.oscillators.WaveOscillator;

import java.util.Arrays;

class MusicalInstrument implements IMusicalInstrument {

    @Getter
    private String name;
    private WaveOscillator[] oscillators;

    public MusicalInstrument(String name, WaveOscillator[] oscillators) {
        this.name = name;
        this.oscillators = oscillators;
        for(WaveOscillator oscillator: oscillators){
            oscillator.setDaemon(true);
            oscillator.start();
        }
    }

    public boolean play(float pitch) {
        boolean pitchAlreadyPlaying = Arrays.stream(oscillators).anyMatch(osc -> osc.getPitch() == pitch);
        if(pitchAlreadyPlaying){
            return false;
        }
        for (WaveOscillator oscillator : oscillators) {
            if (!oscillator.isBusy()) {
                oscillator.setBusy(true);
                oscillator.setPitch(pitch);
                return true;
            }
        }
        return false;
    }

    public boolean stop(float pitch) {
        for (WaveOscillator oscillator : oscillators) {
            if(oscillator.getPitch() == pitch){
                oscillator.setPitch(0.0f);
                oscillator.setBusy(false);
                return true;
            }
        }
        return false;
    }
}

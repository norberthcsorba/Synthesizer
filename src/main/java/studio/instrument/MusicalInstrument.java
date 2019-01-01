package studio.instrument;

import lombok.Getter;
import studio.oscillators.WaveOscillator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

class MusicalInstrument implements IMusicalInstrument {

    @Getter
    private String name;
    private List<WaveOscillator> oscillators;

    MusicalInstrument(String name, List<WaveOscillator> oscillators) {
        this.name = name;
        this.oscillators = oscillators;
        oscillators.forEach(oscillator -> {
            oscillator.setDaemon(true);
            oscillator.start();
        });
    }

    public boolean play(float pitch) {
        boolean pitchAlreadyPlaying = oscillators.stream().anyMatch(osc -> osc.getPitch() == pitch);
        if (pitchAlreadyPlaying) {
            return false;
        }
        Optional<WaveOscillator> availableOscillator = oscillators.stream()
                .filter(osc -> !osc.isBusy())
                .findAny();
        if (!availableOscillator.isPresent()) {
            return false;
        }
        availableOscillator.get().setPitch(pitch);
        return true;
    }

    public boolean stop(float pitch) {
        Optional<WaveOscillator> playingOscillator = oscillators.stream()
                .filter(osc -> osc.getPitch() == pitch)
                .findAny();
        if (!playingOscillator.isPresent()) {
            return false;
        }
        playingOscillator.get().setPitch(0.0f);
        return true;
    }

    @Override
    public void cleanUp() {
        oscillators.forEach(WaveOscillator::cleanUp);
    }
}
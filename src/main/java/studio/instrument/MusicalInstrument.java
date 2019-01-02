package studio.instrument;

import lombok.Getter;

import java.util.List;
import java.util.Optional;

class MusicalInstrument implements IMusicalInstrument {

    @Getter
    private String name;
    private List<InstrumentString> strings;

    MusicalInstrument(String name, List<InstrumentString> strings) {
        this.name = name;
        this.strings = strings;
    }

    public boolean play(float pitch) {
        boolean pitchAlreadyPlaying = strings.stream().anyMatch(osc -> osc.getFundamentalPitch() == pitch);
        if (pitchAlreadyPlaying) {
            return false;
        }
        Optional<InstrumentString> availableString = strings.stream()
                .filter(string -> !string.isBusy())
                .findAny();
        if (!availableString.isPresent()) {
            return false;
        }
        availableString.get().setFundamentalPitch(pitch);
        return true;
    }

    public boolean stop(float pitch) {
        Optional<InstrumentString> playingString = strings.stream()
                .filter(string -> string.getFundamentalPitch() == pitch)
                .findAny();
        if (!playingString.isPresent()) {
            return false;
        }
        playingString.get().setFundamentalPitch(InstrumentString.NULL_PITCH);
        return true;
    }

    @Override
    public void cleanUp() {
        //strings.forEach(WaveOscillator::cleanUp);
    }
}

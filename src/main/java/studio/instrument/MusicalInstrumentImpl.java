package studio.instrument;

import lombok.Getter;

import java.util.List;
import java.util.Optional;

class MusicalInstrumentImpl implements MusicalInstrument {

    @Getter
    private String name;
    @Getter
    private boolean bypass;
    private List<InstrumentString> strings;

    MusicalInstrumentImpl(String name, List<InstrumentString> strings) {
        this.name = name;
        this.strings = strings;
    }

    public boolean startPlaying(float pitch) {
        if(bypass){
            return false;
        }
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

    public boolean stopPlaying(float pitch) {
        if(bypass){
            return false;
        }
        Optional<InstrumentString> playingString = strings.stream()
                .filter(string -> string.getFundamentalPitch() == pitch)
                .findAny();
        if (!playingString.isPresent()) {
            return true;
        }
        playingString.get().setFundamentalPitch(InstrumentString.NULL_PITCH);
        return true;
    }

    @Override
    public void setBypass(boolean bypass) {
        this.bypass = bypass;
        strings.forEach(string -> string.setFundamentalPitch(InstrumentString.NULL_PITCH));
    }

    @Override
    public void cleanUp() {
        //strings.forEach(WaveOscillator::cleanUp);
    }
}

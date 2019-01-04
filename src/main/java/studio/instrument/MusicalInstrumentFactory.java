package studio.instrument;

import lombok.Getter;
import studio.effects.AudioEffect;
import studio.effects.Distortion;
import studio.effects.EnvelopeShaper;
import studio.oscillators.WaveOscillator;
import utils.Constants;
import utils.exceptions.ExceptionMessages;
import utils.exceptions.ObjectInstantiationException;

import javax.sound.sampled.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicalInstrumentFactory {

    @Getter
    private List<Blueprint> availableInstruments;

    public MusicalInstrumentFactory() {
        loadAvailableInstruments();
    }

    public MusicalInstrumentImpl createFromBlueprint(Blueprint blueprint) {
        EnvelopeShaper.setEnvelope(blueprint.getEnvelope());
        return new MusicalInstrumentImpl(blueprint.getName(), newStrings(blueprint));
    }

    private List<WaveOscillator> newOscillatorList(Blueprint blueprint) {
        List<WaveOscillator> oscillators = new ArrayList<>();
        try {
            Class<?> oscClass = Class.forName(waveOscillatorClassName(blueprint.getOscType()));
            for (Float harmonicAmp : blueprint.getHarmonicAmplitudes()) {
                WaveOscillator osc = (WaveOscillator) oscClass.getDeclaredConstructor().newInstance();
                osc.setAmplitude(harmonicAmp);
                oscillators.add(osc);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            throw new ObjectInstantiationException(ExceptionMessages.OSC_COULD_NOT_BE_INSTANTIATED);
        }
        return Collections.unmodifiableList(oscillators);
    }

    private SourceDataLine newOutput() {
        AudioFormat audioFormat = new AudioFormat(Constants.SAMPLE_RATE, Constants.BIT_DEPTH, Constants.NR_OF_CHANNELS,
                Constants.AUDIO_FORMAT_IS_SIGNED, Constants.AUDIO_FORMAT_IS_BIG_ENDIAN);
        SourceDataLine output = null;
        try {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            output = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            output.open(audioFormat, Constants.SIZE_OF_TARGET_BUFFER);
            output.start();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
        return output;
    }

    private List<InstrumentString> newStrings(Blueprint blueprint) {
        List<InstrumentString> strings = new ArrayList<>();
        for (int i = 0; i < blueprint.getPolyphony(); i++) {
            SourceDataLine output = newOutput();
            List<WaveOscillator> oscillators = newOscillatorList(blueprint);
            List<AudioEffect> effects = newAudioEffectList(output);
            InstrumentString string = new InstrumentString(output, oscillators, effects);
            effects.stream()
                    .filter(effect -> effect instanceof EnvelopeShaper)
                    .forEach(effect -> ((EnvelopeShaper) effect).setInstrumentString(string));
            string.setDaemon(true);
            string.start();
            strings.add(string);
        }
        return Collections.unmodifiableList(strings);
    }

    private List<AudioEffect> newAudioEffectList(SourceDataLine output) {
        List<AudioEffect> audioEffectList = new ArrayList<>();
        audioEffectList.add(new EnvelopeShaper(output));
        audioEffectList.add(new Distortion(output));
        return Collections.unmodifiableList(audioEffectList);
    }

    private void loadAvailableInstruments() {
        this.availableInstruments = new ArrayList<>();
        File instrumentsFolder = new File(Constants.INSTRUMENTS_FOLDER_PATH);
        try {
            File[] files = instrumentsFolder.listFiles();
            for (File file : files) {
                Blueprint blueprint = MusicalInstrumentBlueprintParser.parseXmlBlueprintFile(file);
                if (blueprint != null) {
                    this.availableInstruments.add(blueprint);
                }
            }
        } catch (NullPointerException ex) {
            throw new ObjectInstantiationException(ExceptionMessages.INSTRUMENTS_FOLDER_NOT_FOUND_OR_EMPTY);
        }

    }

    private String waveOscillatorClassName(String oscType) {
        StringBuilder className = new StringBuilder(oscType);
        Character firstLetter = className.charAt(0);
        className.replace(0, 1, firstLetter.toString().toUpperCase());
        className.append("WaveOscillator");
        className.insert(0, "studio.oscillators.");
        return className.toString();
    }

}

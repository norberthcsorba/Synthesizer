package studio.instrument;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Blueprint {
    private String name;
    private String oscType;
    private byte polyphony;
    private List<Float> harmonicAmplitudes;
    private EnvelopeShaper.Envelope envelope;
}

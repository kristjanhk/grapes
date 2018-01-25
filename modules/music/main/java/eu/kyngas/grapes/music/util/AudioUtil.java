package eu.kyngas.grapes.music.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class AudioUtil {
  public static final String MIXER_INDEX = "mixerIndex";

  public static AudioFormat getAudioFormat() {
    return new AudioFormat(44100,
        16,
        2,
        true,
        false);
  }

  public static SourceDataLine startAudioPlayback(int mixerIndex) throws LineUnavailableException {
    Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[mixerIndex]);
    AudioFormat audioFormat = getAudioFormat();
    SourceDataLine sourceDataLine =
        (SourceDataLine) mixer.getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
    sourceDataLine.open(audioFormat);
    sourceDataLine.start();
    return sourceDataLine;
  }
}

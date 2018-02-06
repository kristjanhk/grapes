package eu.kyngas.grapes.music.util;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.MPEGMode;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class AudioUtil {
  public static final String MIXER_INDEX = "mixer";

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
    SourceDataLine line = (SourceDataLine) mixer.getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
    line.open(audioFormat);
    line.start();
    return line;
  }

  public static TargetDataLine startAudioRecording(int mixerIndex) throws LineUnavailableException {
    Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[mixerIndex]);
    AudioFormat audioFormat = getAudioFormat();
    TargetDataLine line = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, audioFormat));
    line.open(audioFormat);
    line.start();
    return line;
  }

  public static LameEncoder createEncoder(AudioFormat inputFormat, int bitrate, int quality) {
    return new LameEncoder(inputFormat, bitrate, MPEGMode.STEREO, quality, false);
  }

  public static Handler<Buffer> encode(LameEncoder encoder, Consumer<Buffer> consumer) {
    return fullBuffer -> {
      byte[] buffer = fullBuffer.getBytes();
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] temp = new byte[encoder.getPCMBufferSize()];
      int bytesToEncode = Math.min(temp.length, buffer.length);
      int bytesEncoded;
      int currentPos = 0;
      while ((bytesEncoded = encoder.encodeBuffer(buffer, currentPos, bytesToEncode, temp)) > 0) {
        currentPos += bytesToEncode;
        bytesToEncode = Math.min(temp.length, buffer.length - currentPos);
        output.write(temp, 0, bytesEncoded);
      }
      consumer.accept(Buffer.buffer(output.toByteArray()));
    };
  }
}

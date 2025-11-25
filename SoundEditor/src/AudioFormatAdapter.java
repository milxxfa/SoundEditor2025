import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AudioFormatAdapter {

    public static File adaptToWav(File sourceFile) {
        String name = sourceFile.getName().toLowerCase();

        if (name.endsWith(".wav")) {
            return sourceFile;
        }

        System.out.println("Adapting file: " + name);
        String parentDir = sourceFile.getParent();
        String newName = name.substring(0, name.lastIndexOf('.')) + "_converted.wav";
        File convertedFile = new File(parentDir, newName);

        AudioInputStream sourceStream = null;
        AudioInputStream pcmStream = null;

        try {

            sourceStream = AudioSystem.getAudioInputStream(sourceFile);
            AudioFormat sourceFormat = sourceStream.getFormat();
            System.out.println("Source format: " + sourceFormat);


            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false); // Little Endian


            pcmStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);


            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;


            while ((nRead = pcmStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            byte[] audioBytes = buffer.toByteArray();
            System.out.println("Decoded bytes size: " + audioBytes.length);


            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            AudioInputStream lengthKnownStream = new AudioInputStream(
                    bais,
                    targetFormat,
                    audioBytes.length / targetFormat.getFrameSize()
            );


            AudioSystem.write(lengthKnownStream, AudioFileFormat.Type.WAVE, convertedFile);

            System.out.println("Conversion success: " + convertedFile.getAbsolutePath());
            return convertedFile;

        } catch (Exception e) {
            System.err.println("Conversion Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (sourceStream != null) sourceStream.close();
                if (pcmStream != null) pcmStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sourceFile;
    }
}
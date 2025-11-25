import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AudioEditorLogic {


    private static byte[] clipboardData = null;
    private static AudioFormat clipboardFormat = null;


    private static List<Clip> activeClips = new ArrayList<>();


    public static void copy(File file, int startSeconds, int endSeconds) throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        AudioFormat format = ais.getFormat();

        long bytesPerSecond = (long) (format.getSampleRate() * format.getFrameSize());
        long skipBytes = startSeconds * bytesPerSecond;
        long copyBytes = (endSeconds - startSeconds) * bytesPerSecond;

        byte[] fileData = ais.readAllBytes();

        if (skipBytes >= fileData.length) return;
        if (skipBytes + copyBytes > fileData.length) {
            copyBytes = fileData.length - skipBytes;
        }

        clipboardData = new byte[(int) copyBytes];
        System.arraycopy(fileData, (int) skipBytes, clipboardData, 0, (int) copyBytes);
        clipboardFormat = format;

        ais.close();
    }


    public static File paste(File destinationFile) throws Exception {
        if (clipboardData == null) throw new Exception("Clipboard is empty!");

        AudioInputStream ais = AudioSystem.getAudioInputStream(destinationFile);
        AudioFormat format = ais.getFormat();

        byte[] originalData = ais.readAllBytes();
        byte[] newData = new byte[originalData.length + clipboardData.length];

        System.arraycopy(originalData, 0, newData, 0, originalData.length);
        System.arraycopy(clipboardData, 0, newData, originalData.length, clipboardData.length);

        String newPath = destinationFile.getAbsolutePath().replace(".wav", "_pasted.wav");
        File newFile = new File(newPath);

        ByteArrayInputStream bais = new ByteArrayInputStream(newData);
        AudioInputStream newAis = new AudioInputStream(bais, format, newData.length / format.getFrameSize());
        AudioSystem.write(newAis, AudioFileFormat.Type.WAVE, newFile);

        ais.close();
        newAis.close();
        return newFile;
    }



    public static void playMultiple(List<File> files) {

        stopAll();

        for (File f : files) {
            new Thread(() -> {
                try {
                    Clip clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(f));


                    synchronized (activeClips) {
                        activeClips.add(clip);
                    }

                    clip.start();


                    Thread.sleep(clip.getMicrosecondLength() / 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void stopAll() {
        synchronized (activeClips) {
            for (Clip clip : activeClips) {
                if (clip != null && clip.isRunning()) {
                    clip.stop();
                    clip.close();
                }
            }
            activeClips.clear();
        }
    }
}
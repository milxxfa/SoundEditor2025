import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.File;


public class AudioPlayer implements Player {
    private static AudioPlayer audioPlayer; //A Singleton object

    private AudioPlayer() {
    }

    public static AudioPlayer getInstance() {
        if (audioPlayer == null) {
            audioPlayer = new AudioPlayer();
        }
        return audioPlayer;
    }

    private static final JFrame jf = new JFrame("AudioPlayer");
    private static AudioInputStream audioStream = null;
    private static Clip audioClip = null;

    public void playAudio(File fileToPlay) {

        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(false);

        try {
            audioStream = AudioSystem.getAudioInputStream(fileToPlay);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            audioClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAudio() {
        try {
            audioStream.close();
            audioClip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public interface Pitcher {
    void change(File file) throws IOException, UnsupportedAudioFileException;
}

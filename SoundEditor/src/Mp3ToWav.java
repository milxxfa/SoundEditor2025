import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

import java.io.File;
import java.io.IOException;

public class Mp3ToWav implements WavConverter {

    private static Mp3ToWav mp3ToWav; //A Singleton object

    private Mp3ToWav() {
    }

    public static Mp3ToWav getInstance() {
        if (mp3ToWav == null) {
            mp3ToWav = new Mp3ToWav();
        }
        return mp3ToWav;
    }

    public void convert(String filePath) {
        Converter c = new Converter();
        try {
            String pathToSave = filePath.replace(".mp3", "");
            pathToSave = pathToSave + "-Converted.wav";
            File file = new File(pathToSave);
            file.createNewFile();
            c.convert(filePath, pathToSave);
        } catch (JavaLayerException | IOException e) {
            e.printStackTrace();
        }
    }
}

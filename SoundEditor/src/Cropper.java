import java.io.File;
import java.io.FileNotFoundException;

public interface Cropper {
    public void crop(File file) throws FileNotFoundException;
}

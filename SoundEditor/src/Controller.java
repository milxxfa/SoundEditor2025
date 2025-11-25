public class Controller implements ControllerInterface {
    private static Controller controller;
    private static Player audioPlayer;     //
    private static AudioCrop audioCrop;         //
    private static ChangePitch changePitch;     //Creating a singleton instances
    private static Mp3ToWav mp3ToWav;          //
    private static DataBase dataBase;           //


    private Controller() {
    }

    public static Controller getInstance() {
        if (controller == null) {
            controller = new Controller();
            audioPlayer = AudioPlayer.getInstance();
            audioCrop = AudioCrop.getInstance();
            changePitch = ChangePitch.getInstance();
            mp3ToWav = Mp3ToWav.getInstance();
            dataBase = DataBase.getInstance();
            dataBase.connect();
        }
        return controller;
    }

    public Player getAudioPlayer() {
        return audioPlayer;
    }

    public Cropper getAudioCrop() {
        return audioCrop;
    }

    public Pitcher getChangePitch() {
        return changePitch;
    }

    public WavConverter getMp3ToWav() {
        return mp3ToWav;
    }

    public DataBase getDataBase() {
        return dataBase;
    }
}

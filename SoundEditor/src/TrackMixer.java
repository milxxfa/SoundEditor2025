import java.util.ArrayList;
import java.util.List;

public class TrackMixer implements AudioComponent {
    private List<AudioComponent> tracks = new ArrayList<>();

    public void addTrack(AudioComponent track) {
        tracks.add(track);
    }

    @Override
    public void play() {
        for (AudioComponent track : tracks) {
            new Thread(() -> track.play()).start();
        }
    }

    @Override
    public void stop() {
        for (AudioComponent track : tracks) {
            track.stop();
        }
    }
}

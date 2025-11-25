import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Main {

    private static JFrame f = new JFrame();

    private static JButton btnPlay = new JButton("Play / Stop All");
    private static JButton btnCrop = new JButton("Crop Audio File");
    private static JButton btnChangePitch = new JButton("Change Sample Rate");
    private static JButton btnCopy = new JButton("Copy Segment");
    private static JButton btnPaste = new JButton("Paste to End");
    private static JButton btnAddTrack = new JButton("Add Track");
    private static JButton btnUndo = new JButton("Undo (Back)");
    private static JButton btnSave = new JButton("Save Mix");

    private static JTextPane startTextPane = new JTextPane();
    private static AudioSinWave visualizer = new AudioSinWave();

    private static File selectedFile;
    private static Controller controller;
    private static List<File> trackList = new ArrayList<>();
    private static Stack<File> history = new Stack<>();
    private static int PLAY_FLAG = 0;

    public static void main(String[] args) {
        controller = Controller.getInstance();

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        f.setTitle("Sound Editor - Олександра Биковська");

        JLabel lblStatus = new JLabel("Status: Waiting for file...");
        lblStatus.setForeground(new Color(255, 55, 10));
        lblStatus.setBounds(20, 400, 450, 20);

        JButton btnOpen = new JButton("Open File");

        btnOpen.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Audio Files", "wav", "mp3", "ogg", "flac");
                fileChooser.setFileFilter(filter);
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

                int result = fileChooser.showOpenDialog(f);

                if (result == JFileChooser.APPROVE_OPTION) {
                    lblStatus.setText("Loading & Converting...");
                    history.clear();

                    File rawFile = fileChooser.getSelectedFile();
                    selectedFile = AudioFormatAdapter.adaptToWav(rawFile);

                    if (selectedFile != null) {
                        trackList.clear();
                        trackList.add(selectedFile);
                        visualizer.setFile(selectedFile);

                        Main m = new Main();
                        m.setBtnVisible();
                        lblStatus.setText("Ready: " + selectedFile.getName());
                    } else {
                        lblStatus.setText("Error loading file!");
                    }
                }
            }
        });
        btnOpen.addActionListener(arg0 -> {});
        btnOpen.setBounds(196, 152, 95, 40);
        f.getContentPane().add(btnOpen);

        f.setSize(500, 670);
        f.getContentPane().setLayout(null);
        f.getContentPane().add(lblStatus);

        JTextPane headerText = new JTextPane();
        headerText.setFont(new Font("Tw Cen MT", Font.PLAIN, 16));
        headerText.setBackground(UIManager.getColor("Slider.background"));
        headerText.setText("Sound Editor : Олександра Биковська");
        headerText.setBounds(21, 22, 335, 119);
        f.getContentPane().add(headerText);

        btnPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (PLAY_FLAG == 0) {
                    if (trackList.isEmpty()) {
                        lblStatus.setText("No tracks to play");
                        return;
                    }
                    lblStatus.setText("Playing " + trackList.size() + " track(s)...");
                    AudioEditorLogic.playMultiple(trackList);
                    btnPlay.setText("Stop");
                    PLAY_FLAG = 1;
                } else {
                    lblStatus.setText("Stopped.");
                    AudioEditorLogic.stopAll();
                    btnPlay.setText("Play / Stop All");
                    PLAY_FLAG = 0;
                }
            }
        });
        btnPlay.setBounds(65, 205, 159, 40);
        f.getContentPane().add(btnPlay);

        btnCrop.addActionListener(e -> {
            if (selectedFile != null) {
                history.push(selectedFile);
                try {
                    controller.getAudioCrop().crop(selectedFile);
                    lblStatus.setText("Crop window opened");
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });
        btnCrop.setBounds(273, 205, 142, 40);
        f.getContentPane().add(btnCrop);

        btnChangePitch.addActionListener(e -> {
            if (selectedFile != null) {
                try {
                    controller.getChangePitch().change(selectedFile);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        btnChangePitch.setBounds(65, 255, 159, 40);
        f.getContentPane().add(btnChangePitch);

        btnAddTrack.setBounds(273, 255, 142, 40);
        btnAddTrack.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Audio Files", "wav", "mp3", "ogg", "flac");
            fc.setFileFilter(filter);

            if (fc.showOpenDialog(f) == JFileChooser.APPROVE_OPTION) {
                File rawTrack = fc.getSelectedFile();
                File wavTrack = AudioFormatAdapter.adaptToWav(rawTrack);
                if (wavTrack != null) {
                    trackList.add(wavTrack);
                    lblStatus.setText("Track added! Total: " + trackList.size());
                }
            }
        });
        f.getContentPane().add(btnAddTrack);

        btnCopy.setBounds(65, 305, 159, 40);
        btnCopy.addActionListener(e -> {
            if (selectedFile == null) return;
            try {
                String startStr = JOptionPane.showInputDialog(f, "Start time (seconds):", "0");
                String endStr = JOptionPane.showInputDialog(f, "End time (seconds):", "5");

                if (startStr != null && endStr != null) {
                    int s = Integer.parseInt(startStr);
                    int en = Integer.parseInt(endStr);
                    AudioEditorLogic.copy(selectedFile, s, en);
                    lblStatus.setText("Copied segment [" + s + "-" + en + "s]");
                }
            } catch (Exception ex) {
                lblStatus.setText("Error copying");
            }
        });
        f.getContentPane().add(btnCopy);

        btnPaste.setBounds(273, 305, 142, 40);
        btnPaste.addActionListener(e -> {
            if (selectedFile == null) return;
            history.push(selectedFile);
            try {
                File newFile = AudioEditorLogic.paste(selectedFile);
                selectedFile = newFile;
                visualizer.setFile(selectedFile);
                trackList.set(0, selectedFile);
                lblStatus.setText("Pasted! (Undo available)");
            } catch (Exception ex) {
                lblStatus.setText("Paste failed");
                history.pop();
            }
        });
        f.getContentPane().add(btnPaste);

        btnUndo.setBounds(65, 355, 159, 40);
        btnUndo.addActionListener(e -> {
            if (trackList.size() > 1) {
                trackList.remove(trackList.size() - 1);
                lblStatus.setText("Last track removed. Total: " + trackList.size());
                return;
            }
            if (!history.isEmpty()) {
                selectedFile = history.pop();
                visualizer.setFile(selectedFile);
                trackList.set(0, selectedFile);
                lblStatus.setText("Undone! Back to: " + selectedFile.getName());
            } else {
                lblStatus.setText("Nothing to undo.");
            }
        });
        f.getContentPane().add(btnUndo);

        btnSave.setBounds(273, 355, 142, 40);
        btnSave.addActionListener(e -> {
            if (selectedFile == null) return;

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setDialogTitle("Save Output File");

            FileNameExtensionFilter wavFilter = new FileNameExtensionFilter("WAVE Audio (*.wav)", "wav");
            FileNameExtensionFilter mp3Filter = new FileNameExtensionFilter("MP3 Audio (*.mp3)", "mp3");
            FileNameExtensionFilter flacFilter = new FileNameExtensionFilter("FLAC Audio (*.flac)", "flac");
            FileNameExtensionFilter oggFilter = new FileNameExtensionFilter("OGG Vorbis (*.ogg)", "ogg");

            fileChooser.addChoosableFileFilter(wavFilter);
            fileChooser.addChoosableFileFilter(mp3Filter);
            fileChooser.addChoosableFileFilter(flacFilter);
            fileChooser.addChoosableFileFilter(oggFilter);

            fileChooser.setFileFilter(wavFilter);
            fileChooser.setSelectedFile(new File("MyMix.wav"));

            int userSelection = fileChooser.showSaveDialog(f);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String ext = "wav";

                if (fileChooser.getFileFilter() == mp3Filter) ext = "mp3";
                else if (fileChooser.getFileFilter() == flacFilter) ext = "flac";
                else if (fileChooser.getFileFilter() == oggFilter) ext = "ogg";

                if (!fileToSave.getName().toLowerCase().contains(".")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + "." + ext);
                }

                try {
                    saveAudioFile(selectedFile, fileToSave);
                    lblStatus.setText("Saved to: " + fileToSave.getName());


                    controller.getDataBase().insert(fileToSave);

                    JOptionPane.showMessageDialog(f, "File saved & logged to DB!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblStatus.setText("Save failed.");
                    JOptionPane.showMessageDialog(f, "Error saving file: " + ex.getMessage());
                }
            }
        });
        f.getContentPane().add(btnSave);

        startTextPane.setBackground(UIManager.getColor("ScrollBar.trackForeground"));
        startTextPane.setBounds(107, 236, 214, 10);
        f.getContentPane().add(startTextPane);

        visualizer.setBounds(20, 430, 460, 120);
        f.getContentPane().add(visualizer);

        f.setVisible(true);

        setBtnVisible(false);
    }

    protected void setBtnVisible() {
        setBtnVisible(true);
    }

    protected static void setBtnVisible(boolean visible) {
        startTextPane.setVisible(false);
        btnPlay.setVisible(visible);
        btnChangePitch.setVisible(visible);
        btnCrop.setVisible(visible);
        btnCopy.setVisible(visible);
        btnPaste.setVisible(visible);
        btnAddTrack.setVisible(visible);
        btnUndo.setVisible(visible);
        btnSave.setVisible(visible);
    }

    private static void saveAudioFile(File source, File destination) throws Exception {
        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(source);
            String destName = destination.getName().toLowerCase();

            if (destName.endsWith(".wav")) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, destination);
                return;
            }

            try {
                AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
                AudioFileFormat.Type targetType = null;
                for (AudioFileFormat.Type type : types) {
                    if (destName.endsWith(".ogg") && (type.getExtension().equalsIgnoreCase("ogg") || type.toString().contains("VORBIS"))) {
                        targetType = type;
                        break;
                    }
                    if (destName.endsWith(".flac") && type.getExtension().equalsIgnoreCase("flac")) {
                        targetType = type;
                        break;
                    }
                }
                if (targetType != null) {
                    AudioSystem.write(ais, targetType, destination);
                    return;
                }
            } catch (Exception e) {
            }

            ais.close();
            ais = AudioSystem.getAudioInputStream(source);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, destination);

        } finally {
            if (ais != null) ais.close();
        }
    }
}
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;

public class ChangePitch implements Pitcher {

    private static ChangePitch instance;
    private JTextField textPitchValue;
    private JLabel lblCurrentRate;
    private File fileToEdit;

    private ChangePitch() {}

    public static ChangePitch getInstance() {
        if (instance == null) {
            instance = new ChangePitch();
        }
        return instance;
    }

    public void change(File selectedFile) {
        if (selectedFile == null) return;
        this.fileToEdit = selectedFile;

        JFrame f = new JFrame("Change Speed/Pitch (Deformation)");
        f.getContentPane().setBackground(Color.WHITE);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setResizable(false);
        f.setSize(400, 250);
        f.setLayout(null);
        f.setLocationRelativeTo(null);

        JLabel lblTitle = new JLabel("Current Sample Rate:");
        lblTitle.setBounds(30, 30, 150, 20);
        f.add(lblTitle);

        lblCurrentRate = new JLabel("Loading...");
        lblCurrentRate.setBounds(180, 30, 150, 20);
        lblCurrentRate.setFont(new Font("Arial", Font.BOLD, 14));
        f.add(lblCurrentRate);

        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(selectedFile);
            float rate = ais.getFormat().getSampleRate();
            lblCurrentRate.setText((int)rate + " Hz");
            ais.close();
        } catch (Exception e) {
            lblCurrentRate.setText("Unknown");
        }

        JLabel lblEnter = new JLabel("Enter New Rate (Hz):");
        lblEnter.setBounds(30, 80, 150, 20);
        f.add(lblEnter);

        textPitchValue = new JTextField();
        textPitchValue.setBounds(180, 80, 100, 25);
        f.add(textPitchValue);

        JButton btnApply = new JButton("Apply Deformation");
        btnApply.setBounds(120, 140, 140, 30);
        f.add(btnApply);

        JLabel lblInfo = new JLabel("<html>Examples:<br>60000 = Chipmunk (Fast/High)<br>25000 = Slow Motion (Slow/Low)</html>");
        lblInfo.setBounds(30, 170, 300, 40);
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 10));
        lblInfo.setForeground(Color.GRAY);
        f.add(lblInfo);

        f.setVisible(true);

        btnApply.addActionListener(e -> {
            try {
                String rawInput = textPitchValue.getText();
                String cleanInput = rawInput.replaceAll("[^0-9]", "");

                if (cleanInput.isEmpty()) {
                    JOptionPane.showMessageDialog(f, "Please enter a number!");
                    return;
                }

                int newRate = Integer.parseInt(cleanInput);

                if (newRate < 4000 || newRate > 96000) {
                    JOptionPane.showMessageDialog(f, "Please enter value between 4000 and 96000");
                    return;
                }

                processChange(selectedFile, newRate);
                f.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(f, "Error reading number.");
            }
        });
    }

    private void processChange(File source, int newRate) {
        try {
            AudioInputStream sourceStream = AudioSystem.getAudioInputStream(source);
            AudioFormat sourceFormat = sourceStream.getFormat();


            byte[] audioData = sourceStream.readAllBytes();
            sourceStream.close();


            AudioFormat targetFormat = new AudioFormat(
                    sourceFormat.getEncoding(),
                    (float) newRate,
                    sourceFormat.getSampleSizeInBits(),
                    sourceFormat.getChannels(),
                    sourceFormat.getFrameSize(),
                    (float) newRate,
                    sourceFormat.isBigEndian()
            );


            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream deformedStream = new AudioInputStream(bais, targetFormat, audioData.length / sourceFormat.getFrameSize());


            String newPath = source.getAbsolutePath().replace(".wav", "") + "_pitch" + newRate + ".wav";
            File targetFile = new File(newPath);

            AudioSystem.write(deformedStream, AudioFileFormat.Type.WAVE, targetFile);

            JOptionPane.showMessageDialog(null, "Deformation Success!\nSaved as:\n" + targetFile.getName());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }
}

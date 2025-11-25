import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;


public class AudioSinWave extends JPanel {
    private byte[] audioData;


    public void setFile(File file) {
        if (file == null) return;


        audioData = null;
        repaint();


        new Thread(() -> {
            try {

                AudioInputStream ais = AudioSystem.getAudioInputStream(file);


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = ais.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }

                audioData = baos.toByteArray();
                ais.close();


                SwingUtilities.invokeLater(this::repaint);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);


        g.setColor(new Color(40, 40, 40));
        g.fillRect(0, 0, getWidth(), getHeight());

        if (audioData == null) {
            g.setColor(Color.WHITE);
            g.drawString("No Audio Loaded", 10, 20);
            return;
        }


        g.setColor(new Color(0, 255, 128));

        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;


        int increment = audioData.length / width;
        if (increment < 1) increment = 1;

        for (int i = 0; i < width - 1; i++) {
            int index = i * increment;

            if (index < audioData.length) {

                byte sample = audioData[index];


                int sampleHeight = (sample * height) / 256;


                g.drawLine(i, centerY - sampleHeight, i, centerY + sampleHeight);
            }
        }
    }
}
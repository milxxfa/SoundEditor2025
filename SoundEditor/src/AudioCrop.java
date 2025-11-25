import wavCrop.DataInputStreamLittleEndian;
import wavCrop.WavFormatTest;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class AudioCrop implements Cropper {
    private static AudioCrop audioCrop; //A Singleton object
    Controller controller = Controller.getInstance();

    private AudioCrop() {
    }

    public static AudioCrop getInstance() {
        if (audioCrop == null) {
            audioCrop = new AudioCrop();
        }
        return audioCrop;
    }

    static int time;

    public void crop(File selectedFile) throws FileNotFoundException {
        time = 0;
        JFrame f = new JFrame("Crop Audio");
        JTextField textFieldT1, textFieldT2;
        f.getContentPane().setBackground(Color.WHITE);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setResizable(false);
        f.setAlwaysOnTop(true);
        f.setForeground(Color.WHITE);
        f.setSize(400, 250);
        f.getContentPane().setLayout(null);

        JLabel lblNewLabel = new JLabel("Start Point");
        lblNewLabel.setBounds(47, 27, 62, 14);
        f.getContentPane().add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Stop Point");
        lblNewLabel_1.setBounds(222, 27, 62, 14);
        f.getContentPane().add(lblNewLabel_1);

        textFieldT1 = new JTextField();
        textFieldT1.setBounds(37, 52, 86, 20);
        f.getContentPane().add(textFieldT1);
        textFieldT1.setColumns(10);

        textFieldT2 = new JTextField();
        textFieldT2.setBounds(207, 52, 86, 20);
        f.getContentPane().add(textFieldT2);
        textFieldT2.setColumns(10);

        JTextPane textResultMessage = new JTextPane();
        textResultMessage.setForeground(new Color(50, 205, 50));
        textResultMessage.setBounds(37, 156, 290, 54);
        f.getContentPane().add(textResultMessage);
        f.setVisible(true);


        DataInputStreamLittleEndian r = new DataInputStreamLittleEndian(new DataInputStream(new BufferedInputStream(new FileInputStream(selectedFile.getAbsolutePath()))));
        try {
            byte[] riff = new byte[4];
            r.read(riff);
            int totalSize = r.readInt();
            System.out.println("Total Size = " + totalSize);
            for (int i = 0; i < 5; i++) {
                r.readInt();
            }
            int bps = r.readInt();
            time = totalSize / bps;
            System.out.println("Time = " + time);

            textResultMessage.setText("Time Duration of Audio is " + time + "\nSo, enter crop points between [0-" + time + "]");

        } catch (IOException e1) {
            e1.printStackTrace();
        }


        JButton btnCrop = new JButton("Crop");
        btnCrop.addActionListener(e -> {


            int start = Integer.parseInt(textFieldT1.getText());
            int stop = Integer.parseInt(textFieldT2.getText());

            if (stop <= time) {
                System.out.println("Start edittext = " + start + "stop = " + stop);
                try {
                    WavFormatTest.crop(selectedFile.getPath(), start, stop);
                    String savedFilePath = selectedFile.getPath();
                    savedFilePath = savedFilePath.replace(".wav", "");
                    textResultMessage.setText("Successfully Cropped\nSaved as " + savedFilePath + "-Cropped.wav");
                    File file = new File(savedFilePath + "-Cropped.wav");
                    controller.getDataBase().insert(file);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    textResultMessage.setText("Error..");
                }
            } else {
                textResultMessage.setText("Stop time should be less than " + time + " for this audio file");
            }
        });
        btnCrop.setBounds(131, 122, 89, 23);
        f.getContentPane().add(btnCrop);

        JTextPane textPaneToCrop = new JTextPane();
        textPaneToCrop.setText("Enter Starting time and Ending Time in seconds");
        textPaneToCrop.setBackground(Color.WHITE);
        textPaneToCrop.setBounds(37, 83, 290, 44);
        f.getContentPane().add(textPaneToCrop);
    }
}

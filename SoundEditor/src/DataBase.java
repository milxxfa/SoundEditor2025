import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBase {
    private static DataBase dataBase;

    private DataBase() {
    }

    public static DataBase getInstance() {
        if (dataBase == null) {
            dataBase = new DataBase();
        }
        return dataBase;
    }

    private static Connection dataBaseConnection;
    List<Integer> fileIdList;

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            dataBaseConnection = DriverManager.getConnection("jdbc:sqlite:SoundEditor.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            dataBaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert(File file) {
        try {
            Statement statement = dataBaseConnection.createStatement();
            if (!fileTableLimitCheck()) {
                String deleteOldFormatQuery = "DELETE FROM formats WHERE fileId = " + fileIdList.get(fileIdList.size() - 1);
                statement.executeUpdate(deleteOldFormatQuery);
                String deleteOldFileQuery = "DELETE FROM files WHERE id = " + fileIdList.get(fileIdList.size() - 1);
                statement.executeUpdate(deleteOldFileQuery);
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] fileData;
            fileData = fileInputStream.readAllBytes();
            AudioFormat audioFormat = AudioSystem.getAudioInputStream(file).getFormat();

            String insertFileQuery =
                    "INSERT INTO files(name, audioData)" +
                            "VALUES ('" + file.getName() + "', '" + fileData + "')";

            statement.executeUpdate(insertFileQuery);
            fileTableLimitCheck();

            String insertFormatQuery =
                    "INSERT INTO formats(fileId, encoding, sampleRate, channels, frameSize, frameRate)" +
                            "VALUES ('" + fileIdList.get(fileIdList.size() - 1) + "', '" +
                            audioFormat.getEncoding().toString() + "', '" + audioFormat.getSampleRate() + "', " +
                            "'" + audioFormat.getChannels() + "', '" + audioFormat.getFrameSize() + "', '" +
                            audioFormat.getFrameRate() + "')";

            statement.executeUpdate(insertFormatQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Boolean fileTableLimitCheck() {

        try {
            fileIdList = new ArrayList<>();
            Statement statement = dataBaseConnection.createStatement();
            String quantityQuery = "SELECT id " +
                    "FROM files";
            ResultSet resultSet = statement.executeQuery(quantityQuery);
            if (resultSet != null) {
                while (resultSet.next()) {
                    fileIdList.add(resultSet.getInt("id"));
                }
                return fileIdList.size() < 5;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void printSavedFiles(){
        try {
            Statement statement = dataBaseConnection.createStatement();
            String filesQuery = "SELECT id, name " +
                    "FROM files";
            ResultSet resultSet = statement.executeQuery(filesQuery);
            System.out.println("Files: ");
            if (resultSet != null) {
                while (resultSet.next()) {
                    System.out.println("FileId: " + resultSet.getInt("id") + " "
                            + "Name: " + resultSet.getString("name"));
                }
                System.out.println("\n");
            }
            printSavedFormats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printSavedFormats(){
        try {
            Statement statement = dataBaseConnection.createStatement();
            String formatsQuery = "SELECT * " +
                    "FROM formats";
            ResultSet resultSet = statement.executeQuery(formatsQuery);
            System.out.println("Formats: ");
            if (resultSet != null) {
                while (resultSet.next()) {
                    System.out.println("Id: " + resultSet.getInt("id") + " "
                            + "FileId: " + resultSet.getInt("fileId") + " "
                            + "Encoding: " + resultSet.getString("encoding") + " "
                            + "SampleRate: " + resultSet.getFloat("sampleRate") + " "
                            + "Channels: " + resultSet.getInt("channels") + " "
                            + "FrameSize: " + resultSet.getInt("frameSize") + " "
                            + "FrameRate: " + resultSet.getFloat("frameRate"));
                }
                System.out.println("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

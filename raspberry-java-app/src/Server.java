import javax.sound.sampled.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by hadoop on 9/14/16.
 */
public class Server implements  Runnable {

    @Override
    public void run() {
        Properties prop = new Properties();
        try {
            InputStream inputStream=new FileInputStream("config.properties");
            prop.load(inputStream);
        } catch (FileNotFoundException e) {
            System.out.println("Can't finde config");
        } catch (IOException e) {
            System.out.println("Can't load config");
        }

        float FREQUENCY=Float.parseFloat(prop.getProperty("frequency"));
        int AUDIO_ENCODING=Integer.parseInt(prop.getProperty("audio_encoding"));
        int CHANNAL_SERVER=Integer.parseInt(prop.getProperty("channal_server"));
        int SERVERPORT=Integer.parseInt(prop.getProperty("serverport_server"));
        int BUFFER_SIZE=Integer.parseInt(prop.getProperty("buffer_size"));

        Socket connfd;
        ServerSocket sockfd;
        connfd = new Socket();
        System.out.println("Enter ip of phone");
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        String ip=reader.nextLine();
        //iz audio objekta u niz bita pogodan za slanje
        AudioFormat format = new AudioFormat(FREQUENCY, AUDIO_ENCODING, CHANNAL_SERVER, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine sourceDataLine = null;
        try {
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open();
        } catch (Exception e) {
            System.out.println("Line is busy at the moment you need to reconnect");
            return;
            }


        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            sockfd = new ServerSocket(SERVERPORT);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        while (true) {
            client c=new client(ip,prop);
            try {
                connfd = sockfd.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
            c.stop();
            System.out.println("Receiving...");
            SourceDataLine finalSourceDataLine = sourceDataLine;
            Socket finalConnfd = connfd;

            FloatControl volumeControl = (FloatControl) finalSourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(6.0f);
            try {
                finalSourceDataLine.open(format);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            finalSourceDataLine.start();
            while (true) {
                int readSize = 0;
                try {
                    readSize = finalConnfd.getInputStream().read(buffer);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                if (readSize == -1)
                    break;
                finalSourceDataLine.write(buffer, 0, readSize);
            }
            System.out.println("Stopped receiving");
            /*try {
                finalConnfd.close();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
    }
}

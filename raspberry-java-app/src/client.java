import javax.sound.sampled.*;
import java.awt.*;
import java.lang.annotation.Target;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by hadoop on 9/14/16.
 */
public class client implements Runnable {

    boolean isRecording;
    Socket connfd;

    public client(String ip, Properties prop){

        float FREQUENCY=Float.parseFloat(prop.getProperty("frequency"));
        int AUDIO_ENCODING=Integer.parseInt(prop.getProperty("audio_encoding"));
        int CHANNAL_CLIENT=Integer.parseInt(prop.getProperty("channal_client"));
        int SERVERPORT=Integer.parseInt(prop.getProperty("serverport_client"));
        final int BUFFER_SIZE=Integer.parseInt(prop.getProperty("buffer_size"));

        AudioFormat format = new AudioFormat(FREQUENCY, AUDIO_ENCODING, CHANNAL_CLIENT, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);

        TargetDataLine targetDataLine = null;
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        TargetDataLine finalTargetDataLine = targetDataLine;
        new Thread() {
            byte[] buffer = new byte[BUFFER_SIZE];
            public void run() {
                try {
                    connfd = new Socket(ip, SERVERPORT);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                finalTargetDataLine.start();
                isRecording = true;
                System.out.println("Transmitting...");
                while (isRecording) {
                    int readSize = finalTargetDataLine.read(buffer, 0, BUFFER_SIZE);
                    try {
                        connfd.getOutputStream().write(buffer, 0, readSize);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        System.out.println("Transmitting stopped");
                        break;
                    }
                }
                finalTargetDataLine.stop();
                finalTargetDataLine.flush();
                finalTargetDataLine.close();
                try {
                    connfd.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void run() {

    }

    void stop(){
        isRecording=false;
    }
}

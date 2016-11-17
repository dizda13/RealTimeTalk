package com.example.dizda.api16_rtt;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class MediaStreamClient {

    boolean isRecording;
    int recBufSize;
    //ServerSocket sockfd;
    Socket connfd;
    AudioRecord audioRecord;
    private static final String TAG = "MyActivity";

    public MediaStreamClient(final Context ctx, final String ip) {

        Properties prop = new Properties();
        try {
            InputStream inputStream=new FileInputStream("config.properties");
            prop.load(inputStream);
        } catch (FileNotFoundException e) {
            System.out.println("Can't finde config");
        } catch (IOException e) {
            System.out.println("Can't load config");
        }

        final int frequency = Integer.parseInt(prop.getProperty("frequency"));
        final int channelConfiguration = Integer.parseInt(prop.getProperty("channal_server"));
        final int audioEncoding = Integer.parseInt(prop.getProperty("audio_encoding"));
        final int SERVERPORT = Integer.parseInt(prop.getProperty("serverport_server"));

        recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        //Log.v(TAG,String.valueOf(AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO , AudioFormat.ENCODING_PCM_16BIT)));
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);

        new Thread() {
            byte[] buffer = new byte[recBufSize];

            public void run() {
                try { connfd = new Socket(ip, SERVERPORT); }
                catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.toast("Can't connect!",ctx);
                    return;
                }
                audioRecord.startRecording();
                isRecording = true;
                while (isRecording) {

                    int readSize = audioRecord.read(buffer, 0, recBufSize);

                    try { connfd.getOutputStream().write(buffer, 0, readSize);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        MainActivity.toast("Closed stream by revicer!",ctx);
                        break;
                    }
                }
                audioRecord.stop();
                //audioRecord.release();
                try { connfd.close(); }
                catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.toast("Can't close connection!",ctx);
                }
            }

        }.start();
    }



    public void stop(Context ctx) {
        isRecording = false;
        /*try { connfd.close(); }
        catch (Exception e) {
            e.printStackTrace();
            MainActivity.toast("Can't close connection!",ctx);
        }*/
    }
}
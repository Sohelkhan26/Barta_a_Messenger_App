package com.example.barta_a_messenger_app;

import android.media.MediaRecorder;
import android.os.Environment;
import java.io.IOException;

public class VoiceRecorder {

    private MediaRecorder mediaRecorder;
    private String outputFilePath;

    public VoiceRecorder() {
        outputFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recorded_audio.3gp";
    }

    public void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }
}

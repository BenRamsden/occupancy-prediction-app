package ramsden.benjamin.navigationapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class SensorAudioAsyncTask extends AsyncTask<Void, double[], Boolean> {

    private static final int sampling_rate = 44100;
    private static final int channel_format = AudioFormat.CHANNEL_IN_MONO;
    private static final int encoding_format = AudioFormat.ENCODING_PCM_16BIT;
    private static final int bin_size = 44;

    private AudioRecord audioRecord;
    private double HzPerBin = 0;

    @Override
    protected Boolean doInBackground(Void... params) {
        HzPerBin = sampling_rate / (2 * bin_size);

        RealDoubleFFT realDoubleFFT = new RealDoubleFFT(bin_size);

        int minBufferSize = AudioRecord.getMinBufferSize(sampling_rate, channel_format, encoding_format);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampling_rate, channel_format, encoding_format, minBufferSize);

        try {
            audioRecord.startRecording();

        } catch(IllegalStateException ex) {
            Log.d(Constants.SENSOR_AUDIO, "Failed to audioRecord.startRecording() with ex: " + ex.toString());
        }

        int buffer_size;

        short[] read_buffer = new short[bin_size];

        double[] fft_buffer = new double[bin_size];

        for(int i = 0; i < 50000; i++) {
            Log.d(Constants.SENSOR_AUDIO, "Spectogram analysis iteration " + i);

            buffer_size = audioRecord.read(read_buffer, 0, bin_size);

            for (int x = 0; x < bin_size && x < buffer_size; x++) {
                fft_buffer[x] = (double) read_buffer[x] / 32768.0; // signed 16 bit
            }

            realDoubleFFT.ft(fft_buffer);

            publishProgress(fft_buffer);
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(double[]...progress) {
        Log.d(Constants.SENSOR_AUDIO, "onProgressUpdate");

        for (int bin = 0; bin < progress[0].length; bin++) {

            double vl = Math.abs(progress[0][bin]) * 10;

            if(vl > 1d) {
                Log.d(Constants.SENSOR_AUDIO, "bin: " + bin + " lo: " + HzPerBin*bin + " hi: " + (HzPerBin*(bin+1)-1) + " vl: " + vl);
            }

        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        Log.d(Constants.SENSOR_AUDIO, "onPostExecute");

        try {
            audioRecord.stop();

        } catch(IllegalStateException ex) {
            Log.d(Constants.SENSOR_AUDIO, "Failed to audioRecord.stop() with ex: " + ex.toString());
        }

    }
}
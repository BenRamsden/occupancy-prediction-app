package ramsden.benjamin.navigationapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class RecordAudio extends AsyncTask<Void, double[], Boolean> {

    private static final int frequency = 44100;
    private static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private static final int blockSize = 44;

    private AudioRecord audioRecord;
    private RealDoubleFFT transformer;
    private double HzPerBin = 0;

    @Override
    protected Boolean doInBackground(Void... params) {
        HzPerBin = frequency / (2 * blockSize);

        transformer = new RealDoubleFFT(blockSize);

        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, frequency, channelConfiguration, audioEncoding, bufferSize);

        int bufferReadResult;

        short[] buffer = new short[blockSize];

        double[] toTransform = new double[blockSize];

        try {
            audioRecord.startRecording();
        } catch (IllegalStateException e) {
            Log.e(Constants.SENSOR_AUDIO, e.toString());
        }

        for(int i = 0; i < 50000; i++) {
            Log.d(Constants.SENSOR_AUDIO, "Starting specrogram analysis iteration " + i);

            bufferReadResult = audioRecord.read(buffer, 0, blockSize);

            for (int x = 0; x < blockSize && x < bufferReadResult; x++) {
                toTransform[x] = (double) buffer[x] / 32768.0; // signed 16 bit
            }

            transformer.ft(toTransform);

            publishProgress(toTransform);
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(double[]...progress) {
        Log.e(Constants.SENSOR_AUDIO, "Displaying in progress");

        for (int bin = 0; bin < progress[0].length; bin++) {

            double vl = Math.abs(progress[0][bin]) * 10;

            if(vl > 1d) {
                Log.d(Constants.SENSOR_AUDIO, "bin: " + bin + " Hz: " + HzPerBin*bin + " vl: " + vl);
            }

        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        try {
            audioRecord.stop();
        } catch(IllegalStateException e){
            Log.e(Constants.SENSOR_AUDIO, e.toString());
        }

    }
}
package ramsden.benjamin.navigationapp;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.SynchronousQueue;

import ca.uol.aig.fftpack.RealDoubleFFT;

/**
 * Created by ben on 09/02/2017.
 */

public class SensorAudio {

    SensorAudioAsyncTask sensorAudioAsyncTask;
    Context mContext;
    Location mLocation;

    public SensorAudio(Context context) {
        // Record to the external cache directory for visibility
        mContext = context;

    }

    public void start(Location location) {
        Log.d(Constants.SENSOR_AUDIO, "start");

        if(sensorAudioAsyncTask != null && sensorAudioAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(Constants.SENSOR_AUDIO, "sensorAudioAsyncTask has status RUNNING, refused to start another");
            return;
        }

        mLocation = location;

        sensorAudioAsyncTask = new SensorAudioAsyncTask();

        sensorAudioAsyncTask.execute();
    }


    public class SensorAudioAsyncTask extends AsyncTask<Void, double[], Boolean> {

        private static final int sampling_rate = 44100;
        private static final int channel_format = AudioFormat.CHANNEL_IN_MONO;
        private static final int encoding_format = AudioFormat.ENCODING_PCM_16BIT;
        private static final int bin_size = 44;

        private AudioRecord audioRecord;
        private double HzPerBin = sampling_rate / (2 * bin_size);

        private JSONObject histograms;
        private Integer histograms_size = 0;

        private Integer publishSkipCount = 1000;  //skip the initial all 0s in the buffer

        @Override
        protected Boolean doInBackground(Void... params) {
            histograms = new JSONObject();

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

            for(int i = 0; i < 50000 && histograms_size < 5; i++) {
                //Log.d(Constants.SENSOR_AUDIO, "Spectogram analysis iteration " + i);

                buffer_size = audioRecord.read(read_buffer, 0, bin_size);

                for (int x = 0; x < bin_size && x < buffer_size; x++) {
                    fft_buffer[x] = (double) read_buffer[x] / 32768.0; // signed 16 bit
                }

                realDoubleFFT.ft(fft_buffer);

                if(publishSkipCount == 0) {
                    publishSkipCount = 255;

                    Log.d(Constants.SENSOR_AUDIO, "Spectogram analysis publishProgress iteration: " + i);
                    publishProgress(fft_buffer);
                } else {
                    publishSkipCount--;
                }

            }

            return true;
        }

        @Override
        protected void onProgressUpdate(double[]...progress) {
            Log.d(Constants.SENSOR_AUDIO, "onProgressUpdate");

            JSONObject histogram = new JSONObject();

            for (int bin = 0; bin < progress[0].length; bin++) {

                JSONObject this_bin = new JSONObject();

                double vl = Math.abs(progress[0][bin]) * 10;

                //Log.d(Constants.SENSOR_AUDIO, "bin: " + bin + " lo: " + HzPerBin*bin + " hi: " + (HzPerBin*(bin+1)-1) + " vl: " + vl);

                try {
                    this_bin.put("lo", String.valueOf(HzPerBin*bin));
                    this_bin.put("hi", String.valueOf(HzPerBin*(bin+1)-1));
                    this_bin.put("vl", String.valueOf(vl));
                    histogram.put(String.valueOf(bin), this_bin);
                } catch (JSONException ex) {
                    Log.d(Constants.SENSOR_AUDIO, "JSONException putting values into this_bin ex: " + ex.toString());
                }

            }

            try {
                histograms.put(String.valueOf(histograms_size++), histogram);
            } catch (JSONException ex) {
                Log.d(Constants.SENSOR_AUDIO, "JSONException putting histogram into histograms ex: " + ex.toString());
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            Log.d(Constants.SENSOR_AUDIO, "onPostExecute");

            String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

            ContentValues audioValues = new ContentValues();
            audioValues.put(NavigationContract.AudioObservations.KEY_LATITUDE, mLocation.getLatitude());
            audioValues.put(NavigationContract.AudioObservations.KEY_LONGITUDE, mLocation.getLongitude());
            audioValues.put(NavigationContract.AudioObservations.KEY_AUDIO_HISTOGRAM, histograms.toString());
            audioValues.put(NavigationContract.AudioObservations.KEY_OBSERVATION_DATE, current_date);
            Uri audioUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.AudioObservations.TABLE_NAME);
            mContext.getContentResolver().insert(audioUri, audioValues);

            Log.d(Constants.SENSOR_AUDIO, "Sent audio_histogram: " + histograms.toString());

            try {
                audioRecord.stop();

            } catch(IllegalStateException ex) {
                Log.d(Constants.SENSOR_AUDIO, "Failed to audioRecord.stop() with ex: " + ex.toString());
            }

        }
    }

}
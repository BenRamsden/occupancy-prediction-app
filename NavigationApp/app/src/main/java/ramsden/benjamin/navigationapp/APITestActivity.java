package ramsden.benjamin.navigationapp;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class APITestActivity extends AppCompatActivity {

    private final Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            result_text_view.setText("Response: " + response.toString());
        }
    };

    private final Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            result_text_view.setText("Error: "+error);
            error.printStackTrace(System.out);
        }
    };

    private final View.OnClickListener netButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button clicked_button = (Button) v;
            String observation = clicked_button.getText().toString().toLowerCase();

            if(post_on_get_off_switch.isChecked()) {
                API_POST_Observation(observation, testJSONObject);
            } else {
                API_GET_Observations(observation);
            }
        }
    };

    private List<String> valid_observation = Arrays.asList("audio", "hotspot", "bluetooth", "crowd", "accelerometer");
    private final String api_root = "http://54.154.109.216:3000";
    private final String apitoken = "koH6a1UC71rTDM1LKppXeKYJ54cjc8nIfuJAKPly1GDYpjMMLLCuK5LBp3fXAEkCcID1jCh5pCQp9D8DmCWhJHQlLcUcy4gD68Qy";

    private JSONObject testJSONObject = new JSONObject();

    private ToggleButton post_on_get_off_switch;
    private TextView result_text_view;
    private RequestQueue apiRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apitest);

        try {
            testJSONObject.put("lat","10.234");
            testJSONObject.put("lng","5.435");
            testJSONObject.put("number_connected","10");
            testJSONObject.put("observation_date","2017-02-05 00:36:22");
            testJSONObject.put("occupancy_estimate","23");
            testJSONObject.put("bluetooth_count","5");
            testJSONObject.put("acceleration_timeline", new JSONObject().put("0","12.3").put("1","13.5"));

            JSONObject audio_histogram = new JSONObject();
            audio_histogram.put("0", new JSONObject().put("lo",500).put("hi",999).put("vl",22));
            audio_histogram.put("1", new JSONObject().put("lo",0).put("hi",499).put("vl",34));
            testJSONObject.put("audio_histogram",audio_histogram);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Cache cache = new NoCache();
        Network network = new BasicNetwork(new HurlStack());
        apiRequestQueue = new RequestQueue(cache, network);
        apiRequestQueue.start();

        Button audio_button = (Button) findViewById(R.id.audio_button);
        audio_button.setOnClickListener(netButtonListener);

        Button hotspot_button = (Button) findViewById(R.id.hotspot_button);
        hotspot_button.setOnClickListener(netButtonListener);

        Button bluetooth_button = (Button) findViewById(R.id.bluetooth_button);
        bluetooth_button.setOnClickListener(netButtonListener);

        Button crowd_button = (Button) findViewById(R.id.crowd_button);
        crowd_button.setOnClickListener(netButtonListener);

        Button accelerometer_button = (Button) findViewById(R.id.accelerometer_button);
        accelerometer_button.setOnClickListener(netButtonListener);

        post_on_get_off_switch = (ToggleButton) findViewById(R.id.post_on_get_off_switch);
        result_text_view = (TextView) findViewById(R.id.result_text_view);
    }

    private void API_GET_Observations(String observation) {
        Toast.makeText(APITestActivity.this, "Requesting GET observation: "+observation, Toast.LENGTH_SHORT).show();

        getContentResolver().query(Uri.parse(NavigationContentProvider.CONTENT_URI+"/"+observation+"_observations"), null, null, null, null);
    }

    private void API_POST_Observation(String observation, JSONObject body) {

        ContentValues contentValues = new ContentValues();

        try {
            contentValues.put("lat","10.234");
            contentValues.put("lng","5.435");
            contentValues.put("number_connected","10");
            contentValues.put("observation_date","2017-02-05 00:36:22");
            contentValues.put("occupancy_estimate","23");
            contentValues.put("bluetooth_count","5");
            contentValues.put("acceleration_timeline", new JSONObject().put("0","12.3").put("1","13.5").toString());

            JSONObject audio_histogram = new JSONObject();
            audio_histogram.put("0", new JSONObject().put("lo",500).put("hi",999).put("vl",22));
            audio_histogram.put("1", new JSONObject().put("lo",0).put("hi",499).put("vl",34));
            contentValues.put("audio_histogram",audio_histogram.toString());
        } catch(JSONException ex) {

        }

        getContentResolver().insert(Uri.parse(NavigationContentProvider.CONTENT_URI+"/"+observation+"_observations"), contentValues);

    }
}

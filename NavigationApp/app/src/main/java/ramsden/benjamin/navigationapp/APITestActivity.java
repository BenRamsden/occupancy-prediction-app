package ramsden.benjamin.navigationapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NoCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
            //testJSONObject.put("lat","10.234");
            //testJSONObject.put("lng","5.435");
            testJSONObject.put("number_connected","10");
            //testJSONObject.put("observation_date","2017-02-05 00:36:22");
            ///testJSONObject.put("audio_histogram","{{\"lo\":\"0\",\"hi\":\"500\",\"vl\":\"23.4\"},{\"lo\":\"0\",\"hi\":\"500\",\"vl\":\"23.4\"}}");
            //testJSONObject.put("occupancy_estimate","23");
            //testJSONObject.put("bluetooth_count","5");
            //testJSONObject.put("acceleration_timeline","{\"1.02\",\"3.5\"}");

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

        if(!valid_observation.contains(observation)) {
            Toast.makeText(this,"API_GET_Observations called with unsupported observation type: "+observation, Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                api_root+"/observations/"+observation+"?apitoken="+apitoken,
                null,
                responseListener,
                errorListener
        );

        apiRequestQueue.add(jsonRequest);
    }

    private void API_POST_Observation(String observation, JSONObject body) {
        Toast.makeText(APITestActivity.this, "Requesting POST observation: "+observation, Toast.LENGTH_SHORT).show();

        if(!valid_observation.contains(observation)) {
            Toast.makeText(this,"API_POST_Observation called with unsupported observation type: "+observation, Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.POST,
                api_root+"/observations/"+observation+"?apitoken="+apitoken,
                body,
                responseListener,
                errorListener
        );

        apiRequestQueue.add(jsonRequest);
    }
}
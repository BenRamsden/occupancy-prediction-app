package ramsden.benjamin.navigationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SentLogActivity extends AppCompatActivity {

    public static final String SENT_LOG_RECEIVER = "ramsden.benjamin.navigationapp.SentLogReceiver";
    public static final String SENT_EXTRA_FIELD = "sent";

    private SentLogReceiver sentLogReceiver;

    class SentLogReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.SENT_LOG_ACTIVITY, "SentLogReceiver received an intent");

            if(arrayAdapter == null) {
                Log.d(Constants.SENT_LOG_ACTIVITY, "Could not display sent_log_intent arrayAdapter is null");
                return;
            }

            if(intent == null || !intent.hasExtra(SENT_EXTRA_FIELD)) {
                Log.d(Constants.SENT_LOG_ACTIVITY, "Could not display sent_log_intent intent is null, or extra field is null");
                return;
            }

            /* TODO: log intents in memory in background, and show all intents even if activity not open on receive */
            arrayAdapter.add(intent.getStringExtra(SENT_EXTRA_FIELD));

        }
    }

    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_log);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        listView = (ListView) findViewById(R.id.sent_log_list_view);
        listView.setAdapter(arrayAdapter);

        sentLogReceiver = new SentLogReceiver();

        registerReceiver(sentLogReceiver, new IntentFilter(SENT_LOG_RECEIVER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(sentLogReceiver);
    }

}

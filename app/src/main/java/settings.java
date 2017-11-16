package nu.paheco.mqtt_Notifier;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.MqttCallback;

import nu.paheco.mqtt_Notifier.MainActivity;
import nu.paheco.mqtt_Notifier.R;

/**
 * Created by patrik on 2017-11-05.
 */

public class settings  extends MainActivity{

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

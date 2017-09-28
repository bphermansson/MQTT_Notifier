package nu.paheco.mqtt_Notifier;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private static final String TAG = "MainActivity";
    SharedPreferences sharedpreferences;
    String mqttip, mqttport, mqttuser, mqttpass, mqtttopic, ac, sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView ip = (TextView) findViewById(R.id.mqttip);
        TextView port = (TextView) findViewById(R.id.mqttport);
        TextView user = (TextView) findViewById(R.id.mqttuser);
        TextView pass = (TextView) findViewById(R.id.mqttpass);
        TextView topic = (TextView) findViewById(R.id.mqtt_topic);

        // Get stored preferences
        sharedpreferences = getSharedPreferences("mypref",
                Context.MODE_PRIVATE);
        if (sharedpreferences.contains("ip")) {
            mqttip=sharedpreferences.getString("ip","");
            Log.d(TAG,mqttip);
        }
        else {
            mqttip="192.168.1.1";

        }
        if (sharedpreferences.contains("port")) {
            mqttport = sharedpreferences.getString("port","");
            Log.d(TAG,mqttport);
        }
        else {
            mqttport="1883";
        }
        if (sharedpreferences.contains("user")) {
            mqttuser = sharedpreferences.getString("user","");
            Log.d(TAG,mqttuser);
        }
        else {
            mqttuser="";
        }
        if (sharedpreferences.contains("pass")) {
            mqttpass = sharedpreferences.getString("pass","");
            Log.d(TAG,mqttpass);
        }
        else {
            mqttpass="";
        }
        if (sharedpreferences.contains("topic")) {
            mqtttopic = sharedpreferences.getString("topic","");
            Log.d(TAG,mqtttopic);
        }
        else {
            mqtttopic="";
        }
        if (sharedpreferences.contains("ac")) {
            ac = sharedpreferences.getString("ac","");
            Log.d(TAG,"Autoconnect: " + ac);
            if (ac.equals("true")) {
                CheckBox ac = (CheckBox) findViewById(R.id.chkAuto);
                ac.setChecked(true);
            }
        }
        if (sharedpreferences.contains("sound")) {
            sound = sharedpreferences.getString("sound","");
            Log.d(TAG,"Sound: " + sound);
            if (sound.equals("true")) {
                CheckBox chkSound = (CheckBox) findViewById(R.id.chkSound);
                chkSound.setChecked(true);
            }
        }
        /*
        else {
            mqttpass="";
        }*/

        ip.setText(mqttip);
        port.setText(mqttport);
        user.setText(mqttuser);
        pass.setText(mqttpass);
        topic.setText(mqtttopic);

        CheckBox ac = (CheckBox) findViewById(R.id.chkAuto);
        if (ac.isChecked()){
            connect();
        }


    }

    public void connect(){
        Log.d(TAG, "In connect");

        // Get current values
        TextView ip = (TextView) findViewById(R.id.mqttip);
        TextView port = (TextView) findViewById(R.id.mqttport);
        TextView user = (TextView) findViewById(R.id.mqttuser);
        TextView pass = (TextView) findViewById(R.id.mqttpass);
        TextView tvtopic = (TextView) findViewById(R.id.mqtt_topic);

        TextView tv = (TextView) findViewById(R.id.status);
        tv.setText("Connecting... ");

        mqttip = ip.getText().toString();
        if (mqttip.length()==0) {
            mqttip="192.168.1.1";
        }
       // mqttip="192.168.1.79";
        mqttport = port.getText().toString();
        mqttuser = user.getText().toString();
        mqttpass = pass.getText().toString();
        mqtttopic = tvtopic.getText().toString();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setUserName(mqttuser);
        options.setPassword(mqttpass.toCharArray());
        String address = "tcp://" + mqttip + ":" + mqttport;

        Log.d(TAG,address);
        Log.d(TAG,user+"-"+pass+"-"+mqtttopic);

        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(this.getApplicationContext(), address,
                        clientId);

        try {
            Log.d(TAG,"Connect");
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    Toast.makeText(MainActivity.this, "Connection successful", Toast.LENGTH_SHORT).show();
                    client.setCallback(MainActivity.this);

                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(mqtttopic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // successfully subscribed
                                Toast.makeText(MainActivity.this, "Successfully subscribed to: " + mqtttopic, Toast.LENGTH_SHORT).show();
                                TextView tv = (TextView) findViewById(R.id.status);
                                tv.setText("Successfully subscribed to: " + mqtttopic);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Toast.makeText(MainActivity.this, "Couldn't subscribe to: " + mqtttopic, Toast.LENGTH_SHORT).show();
                                TextView tv = (TextView) findViewById(R.id.status);
                                tv.setText("Couldn't subscribe to: " + mqtttopic);
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    TextView tv = (TextView) findViewById(R.id.status);
                    tv.setText("Connection failed, check your settings.");

                }
            });
        } catch (MqttException e) {
            Log.d(TAG,"Connection failed");
            tv.setText("Connection failed, check your settings.");
            e.printStackTrace();
        }
    }

    public void sendnotification(String title, String text) {
        // https://www.tutorialspoint.com/android/android_notifications.htm
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.notification_icon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);

        CheckBox chkSound = (CheckBox) findViewById(R.id.chkSound);
        if (chkSound.getText()=="false") {
            Log.d(TAG, "Sound on");
            System.out.println("Sound on");
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
        }
        else {
            System.out.println("Sound off");
        }
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        Integer notificationID = 1;
        mNotificationManager.notify(notificationID, mBuilder.build());


    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        TextView status = (TextView) findViewById(R.id.status);
        TextView tv = (TextView) findViewById(R.id.messages);
        String appname = getApplicationName(this);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String currTime = sdf.format(new Date());

        Log.d(appname, currTime + " : " + message.toString());

        tv.append(currTime + " : " + message.toString());
        tv.append(System.getProperty("line.separator"));

        Toast.makeText(MainActivity.this, "Topic: " + topic + "\nMessage: " + message, Toast.LENGTH_LONG).show();
        status.setText("Got a new message");
        sendnotification(topic, message.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void chkSoundClicked(View view) {
        CheckBox chkSound = (CheckBox) findViewById(R.id.chkSound);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (chkSound.isChecked()){
            editor.putString("sound", "true");
        }
        else {
            editor.putString("sound", "false");
        }
        editor.commit();

    }

    public void chkAutoClicked(View view) {
        System.out.println("chkAutoClicked");
        CheckBox chkAC = (CheckBox) findViewById(R.id.chkAuto);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (chkAC.isChecked()){
            editor.putString("ac", "true");
        }
        else {
            editor.putString("ac", "false");
        }
        editor.commit();

    }

    public void btnClick(View view) {
        TextView txtStatus = (TextView) findViewById(R.id.status);
        TextView txtMessages = (TextView) findViewById(R.id.messages);
        //System.out.println("btnClick");

        switch (view.getId()) {
            case R.id.btnClear:
                txtStatus.setText("Clear");
                txtMessages.setText("");
                break;
            case R.id.btnConnect:
                //Log.d(TAG, "Connect clicked");
                TextView ip = (TextView) findViewById(R.id.mqttip);
                TextView port = (TextView) findViewById(R.id.mqttport);
                TextView user = (TextView) findViewById(R.id.mqttuser);
                TextView pass = (TextView) findViewById(R.id.mqttpass);
                TextView topic = (TextView) findViewById(R.id.mqtt_topic);

                String sip = ip.getText().toString();
                String sport = port.getText().toString();
                String suser = user.getText().toString();
                String spass = pass.getText().toString();
                String stopic = topic.getText().toString();

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("ip", sip);
                editor.putString("port", sport);
                editor.putString("user",suser);
                editor.putString("pass",spass);
                editor.putString("topic",stopic);
                editor.commit();

                connect();

        }
    }
    public void etClick (View view) {
        switch (view.getId()) {
            case R.id.mqttip:
                EditText etip = (EditText) findViewById(R.id.mqttip);
                etip.setText("");
                break;
            case R.id.mqttport:
                EditText etport = (EditText) findViewById(R.id.mqttport);
                etport.setText("");
                break;
            case R.id.mqttuser:
                EditText etuser = (EditText) findViewById(R.id.mqttuser);
                etuser.setText("");
                break;
            case R.id.mqttpass:
                EditText etpass = (EditText) findViewById(R.id.mqttpass);
                etpass.setText("");
                break;
            case R.id.mqtt_topic:
                EditText ettopic = (EditText) findViewById(R.id.mqtt_topic);
                ettopic.setText("");
                break;
        }
    }
    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

}
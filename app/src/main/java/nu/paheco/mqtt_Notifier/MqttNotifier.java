package nu.paheco.mqtt_Notifier;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
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

import static android.app.Service.START_STICKY;
import static android.content.ContentValues.TAG;
import static nu.paheco.mqtt_Notifier.MainActivity.getApplicationName;

public class MqttNotifier extends Service implements MqttCallback {

    SharedPreferences sharedpreferences;
    String mqttip, mqttport, mqttuser, mqttpass, mqtttopic, ac, sound;

    IBinder mBinder;      // interface for clients that bind

    public MqttNotifier() {
    }

    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;

    }

    private int DELAY = 2000;
    Handler mHandler = new Handler();
    Runnable toastRunnable;

    public void doToast()
    {
        mHandler.postDelayed(getToastRunnable(), DELAY);
        Toast.makeText(this, "Service!", Toast.LENGTH_SHORT).show();

    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        getSettings();
        connect();
        //doToast();
        return START_STICKY;
    }

    public void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setUserName(mqttuser);
        options.setPassword(mqttpass.toCharArray());
        String address = "tcp://" + mqttip + ":" + mqttport;
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
                    Toast.makeText(MqttNotifier.this, "Connection successful", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
                    client.setCallback(MqttNotifier.this);

                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(mqtttopic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // successfully subscribed
                                Toast.makeText(MqttNotifier.this, "Successfully subscribed to: " + mqtttopic, Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Toast.makeText(MqttNotifier.this, "Couldn't subscribe to: " + mqtttopic, Toast.LENGTH_SHORT).show();
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
                    //Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MqttNotifier.this,"Connection failed",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            Log.d(TAG,"Connection failed");
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String appname = getApplicationName(this);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String currTime = sdf.format(new Date());

        Log.d(appname, currTime + " : " + message.toString());


         Toast.makeText(MqttNotifier.this, "Topic: " + topic + "\nMessage: " + message, Toast.LENGTH_LONG).show();
        //status.setText("Got a new message");
        sendnotification(topic, message.toString());


    }
    public void sendnotification(String title, String text) {
        // https://www.tutorialspoint.com/android/android_notifications.htm
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.notification_icon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        Integer notificationID = 1;
        mNotificationManager.notify(notificationID, mBuilder.build());

    }


        @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
    @Override
    public void connectionLost(Throwable cause) {

    }

    public void getSettings() {
        // Get stored preferences
        sharedpreferences = getSharedPreferences("mypref",
                Context.MODE_PRIVATE);
        if (sharedpreferences.contains("ip")) {
            mqttip=sharedpreferences.getString("ip","");
        }
        else {
            mqttip="192.168.1.1";
        }
        if (sharedpreferences.contains("port")) {
            mqttport = sharedpreferences.getString("port","");
        }
        else {
            mqttport="1883";
        }
        if (sharedpreferences.contains("user")) {
            mqttuser = sharedpreferences.getString("user","");
        }
        else {
            mqttuser="";
        }
        if (sharedpreferences.contains("pass")) {
            mqttpass = sharedpreferences.getString("pass","");
        }
        else {
            mqttpass="";
        }
        if (sharedpreferences.contains("topic")) {
            mqtttopic = sharedpreferences.getString("topic","");
        }
        else {
            mqtttopic="";
        }
    }

    @Override public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
        super.onDestroy();
        mHandler.removeCallbacks(toastRunnable);
    }

    private Runnable getToastRunnable() {
        Runnable r = new Runnable() {
            @Override public void run() {
                // show toast
                doToast();
            }
        };
        toastRunnable = r;
        return r;
    }
}

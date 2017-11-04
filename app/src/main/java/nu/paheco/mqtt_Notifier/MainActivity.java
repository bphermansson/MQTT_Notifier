package nu.paheco.mqtt_Notifier;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.security.AccessController.getContext;

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
        //final TextInputLayout pass = (TextInputLayout) findViewById(R.id.mqttpass);
        //TextView topic = (TextView) findViewById(R.id.mqtt_topic);
        TextView messages = (TextView) findViewById(R.id.messages);
        messages.setMovementMethod(new ScrollingMovementMethod());  // Activate scroll

        Spinner spinner = (Spinner) findViewById(R.id.topic_spinner);

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

        // Clear topicSet
        //sharedpreferences.edit().remove("topicSet").commit();


        // Print all stored preferences
        Map<String, ?> allEntries = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            System.out.println("map values: " + entry.getKey() + ": " + entry.getValue().toString());
        }

        // List with previous topics, autocomplete

        Set<String> outSet = sharedpreferences.getStringSet("topicSet", new HashSet<String>());
        Set<String> workingSet = new HashSet<String>(outSet);
        String oldtopics[] = workingSet.toArray(new String[workingSet.size()]);
/*
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, oldtopics);
        if (oldtopics.length!=0) {
            System.out.println("OT: " + oldtopics[0]);
            System.out.println("OT: " + oldtopics[1]);

        }
*/
        AutoCompleteTextView topicView;
        ArrayAdapter<String> adapterTopic;
        topicView = (AutoCompleteTextView) findViewById(R.id.mqtt_topic);
        adapterTopic = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, oldtopics);
        topicView.setAdapter(adapterTopic);

        // Spinner with ols topics
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, oldtopics);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);

        /*
        else {
            mqttpass="";
        }*/

        ip.setText(mqttip);
        port.setText(mqttport);
        user.setText(mqttuser);
        pass.setText(mqttpass);
        topicView.setText(mqtttopic);

        CheckBox ac = (CheckBox) findViewById(R.id.chkAuto);
        if (ac.isChecked()){
            connect();
        }


    }
    private static final String[] topics = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.about:
                //startActivity(new Intent(this, About.class));
                return true;
            case R.id.help:
                //startActivity(new Intent(this, Help.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

        //boolean checked = ((CheckBox) view).isChecked();
        boolean chkd = chkSound.isChecked();
        //switch(view.getId()) {
          //  case R.id.chkSound:
                if (chkd) {
                    // Put some meat on the sandwich
                } else
                {
                // Remove the meat
                    System.out.println("Sound on");
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    mBuilder.setSound(alarmSound);
                }
            //        break;
        //}
/*
        System.out.println(chkSound.isEnabled());

        if (chkSound.getText().toString()=="Silent")  {
            System.out.println("Sound on");
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
        }
        else {
            System.out.println("Sound off");
        }
        */
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

       // Toast.makeText(MainActivity.this, "Topic: " + topic + "\nMessage: " + message, Toast.LENGTH_LONG).show();
        status.setText("Got a new message");
        sendnotification(topic, message.toString());

        // Clear status textbox
        CountDownTimer timer = new CountDownTimer(1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                TextView status = (TextView) findViewById(R.id.status);
                status.setText(""); //(or GONE)
            }
        }.start();

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
        TextView ip = (TextView) findViewById(R.id.mqttip);
        TextInputLayout ipwrapper = (TextInputLayout) findViewById(R.id.ipWrapper);
        TextView port = (TextView) findViewById(R.id.mqttport);
        TextInputLayout portwrapper = (TextInputLayout) findViewById(R.id.portWrapper);
        TextView user = (TextView) findViewById(R.id.mqttuser);
        TextView pass = (TextView) findViewById(R.id.mqttpass);
        TextInputLayout userwrapper = (TextInputLayout) findViewById(R.id.usernameWrapper);
        TextInputLayout passwrapper = (TextInputLayout) findViewById(R.id.mqttpasswrapper);
        CheckBox cbsound = (CheckBox) findViewById(R.id.chkSound);
        CheckBox cbauto = (CheckBox) findViewById(R.id.chkAuto);
        Button btnSC = (Button) findViewById(R.id.btnConnect);
        AutoCompleteTextView txtTopic = (AutoCompleteTextView) findViewById(R.id.mqtt_topic);
        TextInputLayout topicwrapper = (TextInputLayout) findViewById(R.id.mqtttopicwrapper);
        TextView txtMess = (TextView) findViewById(R.id.messages);
        Spinner topicsspinner = (Spinner) findViewById(R.id.topic_spinner);

        switch (view.getId()) {
            case R.id.btnClear:
                txtStatus.setText("Clear");
                txtMessages.setText("");
                break;
            case R.id.btnConnect:
                //Log.d(TAG, "Connect clicked");
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
/*
                // Get previous topics
                if (sharedpreferences.contains("topic")) {
                    mqtttopic = sharedpreferences.getString("topic","");
                    Log.d(TAG,mqtttopic);
                    System.out.println(mqtttopic);
                }
*/

                Set<String> outSet = sharedpreferences.getStringSet("topicSet", new HashSet<String>());
                Set<String> workingSet = new HashSet<String>(outSet);
                workingSet.add(stopic);
                editor.putStringSet("topicSet", workingSet);

                //editor.putString("topic",stopic);
                editor.commit();

                connect();
                break;

            case R.id.settings:
                System.out.println(ip.getVisibility()); // 0 - visible, 4 - hidden
                Log.d(TAG, Integer.toString(ip.getVisibility()));
                int vis = ip.getVisibility();
                if (vis == 0) {     // Already visible, hide
                    ip.setVisibility(View.INVISIBLE);
                    port.setVisibility(View.INVISIBLE);
                    ipwrapper.setVisibility(View.INVISIBLE);
                    portwrapper.setVisibility(View.INVISIBLE);
                    user.setVisibility(View.INVISIBLE);
                    userwrapper.setVisibility(View.INVISIBLE);
                    pass.setVisibility(View.INVISIBLE);
                    passwrapper.setVisibility(View.INVISIBLE);
                    cbauto.setVisibility(View.INVISIBLE);
                    cbsound.setVisibility(View.INVISIBLE);
                    btnSC.setVisibility(View.INVISIBLE);
                    txtTopic.setVisibility(View.INVISIBLE);
                    txtMessages.setVisibility(View.VISIBLE);
                    txtStatus.setVisibility(View.VISIBLE);
                    topicsspinner.setVisibility(View.INVISIBLE);
                    topicwrapper.setVisibility(View.INVISIBLE);
                }
                else {
                    txtMessages.setVisibility(View.INVISIBLE);
                    ip.setVisibility(View.VISIBLE);
                    port.setVisibility(View.VISIBLE);
                    ipwrapper.setVisibility(View.VISIBLE);
                    portwrapper.setVisibility(View.VISIBLE);
                    user.setVisibility(View.VISIBLE);
                    userwrapper.setVisibility(View.VISIBLE);
                    pass.setVisibility(View.VISIBLE);
                    passwrapper.setVisibility(View.VISIBLE);
                    cbauto.setVisibility(View.VISIBLE);
                    cbsound.setVisibility(View.VISIBLE);
                    btnSC.setVisibility(View.VISIBLE);
                    txtTopic.setVisibility(View.VISIBLE);
                    txtStatus.setVisibility(View.INVISIBLE);
                    topicsspinner.setVisibility(View.VISIBLE);
                    topicwrapper.setVisibility(View.VISIBLE);
                }
                break;

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
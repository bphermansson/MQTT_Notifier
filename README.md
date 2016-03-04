# MQTT-Subscriber-
An app to demonstrate MQTT subscribe using Paho Android MQTT Library .

##Usage 

1.Clone project in Android Studio . 

2.Open MainActivity.java and substitute your MQTT Username and password you got from CloudMQTT Dashboard .(See below) 

3.Download [MQTTLens] (https://chrome.google.com/webstore/detail/mqttlens/hemojaaeigabkbcookmlgmdigohjobjm?hl=en) 

4.In MainActivity.java ,put a topic you want app to subscribe to . (For e.g. door/status) 

5.In MQTTLens ,create a new connection , name it anything you want (say CloudMQTT Broker) and substitute username,password ,URL and Port number as you get from [CloudMQTT Control Pannel](https://api.cloudmqtt.com/sso/cloudmqtt/console)  

6.Now in MQTT Lens , put the same topic in Publish Topic field to which you subscribed to in app in step 4 . 

7.Put "open" and click publish and then "close" and click publish .

##[CloudMQTT](https://www.cloudmqtt.com/)

CloudMQTT is hosted MQTT broker , for testing and learning purpose it's free tier is enough and I find it better compared to using [HiveMQ](broker.hivemq.com) or [Eclipse](iot.eclipse.org). 

Just sign up and opt for free tier plan and get required credentials from [CloudMQTT Control Pannel](https://api.cloudmqtt.com/sso/cloudmqtt/console)




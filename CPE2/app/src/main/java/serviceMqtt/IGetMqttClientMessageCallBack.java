package serviceMqtt;

public interface IGetMqttClientMessageCallBack {
    public void onMqttReceive(String topic,String message);
    public void mqttServiceSecurity(boolean message);
}

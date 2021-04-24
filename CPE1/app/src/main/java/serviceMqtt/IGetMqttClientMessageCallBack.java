package serviceMqtt;

public interface IGetMqttClientMessageCallBack {
    public void setMessage(String topic,String message);
    public void setAllowToSend(boolean allowToSend);
}

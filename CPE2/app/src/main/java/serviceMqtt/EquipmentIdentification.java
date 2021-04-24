package serviceMqtt;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.SharedPreferences;

import java.security.Provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import tool.SignalIntensity;

/**
 * @Author : YangFan
 * @Date : 2020年11月05日 11:52
 * @effect : 设备标识获取
 */

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class EquipmentIdentification{
    private String cpe1Uid;
    private String cpe2Uid;

    /**
     * 获取网址
     */
    public String InitUrl(){
        return "http://47.99.180.14:8080/cpeManager/api/config?cpe1Uid="+cpe1Uid+"&cpe2Uid="+cpe2Uid;
    }
}

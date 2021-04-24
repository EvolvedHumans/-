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

        int integer = 0;

        if(integer == 0){
            return "http://cpetest.dti2018.com/api/config?cpe1Uid="+cpe1Uid+"&cpe2Uid="+cpe2Uid;
        }
        return "http://cpe.dti2018.com/api/config?cpe1Uid="+cpe1Uid+"&cpe2Uid="+cpe2Uid;
    }

}

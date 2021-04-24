package db.agreement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年11月10日 10:42
 * @effect :戴天外网传输数据封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class DataTransmissionProtocol {
    /**
     * 协议类型 1:MQTT,2:UDP,3:TCP,4.HTTP
     */
    private Integer protocolType;

    /**
     * CPE2标识
     */
    private String cpeTwoUid;

    /**
     * 数据标签,仅针对HTTP
     */
    private Long mid;

    /**
     * 推送主题主题名称
     */
    private String topicName;

    /**
     * 发送数据内容
     */
    private String content;

    /**
     * 服务类型
     */
    private Integer qos;

}

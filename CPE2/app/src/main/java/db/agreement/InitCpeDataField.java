package db.agreement;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年11月02日 11:23
 * @effect :InitCpe中data字段的内部封装，这部分字段将直接发给CPE2，CPE2只需要解析后面的便可
 */

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class InitCpeDataField {
    /**
     * CPE1唯一标识
     */
    private String cpe1Uid;

    /**
     * CPE2唯一标识
     */
    private String cpe2Uid;

    /**
     * 协议类型 1:MQTT,2:UDP,3:TCP
     */
    private Integer protocolType;

    /**
     * MQTT连接HOST
     */
    private String host;

    /**
     * MQTT用户名
     */
    private String username;

    /**
     * MQTT密码
     */
    private String password;

    /**
     * MQTT客户端标识
     */
    private String clientId;

    /**
     * 需要订阅的主题名称
     */
    private List<CpeMqttTopicVo> topicList;

    /**
     * TCP或UDP协议IP
     */
    private String ip;

    /**
     * TCP或UDP协议端口
     */
    private Integer port;

    /**
     * HTTP url
     */
    private String url;

    /**
     * 添加一个时间，把cpe1的时间获取到后，给到cpe2
     */
    private Long modernClock;

    /**
     * 时间戳
     */
    private Long timestamp;
}

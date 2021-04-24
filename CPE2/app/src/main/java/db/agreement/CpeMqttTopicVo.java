package db.agreement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年12月17日 15:23
 * @effect : CPE 需要订阅主题的主题名和服务类型存储集合
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class CpeMqttTopicVo {
    private String topic;
    private Integer qos;
}

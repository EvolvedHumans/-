package db.serialPort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年11月12日 14:09
 * @effect : 串口连接验证,第一次推送封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class ConnectionVerificationPublishOne {

    /**
     * SYN = 1 发起连接
     */
    private Integer syn;

    /**
     * seq 推送次数
     */
    private Integer seq;

}

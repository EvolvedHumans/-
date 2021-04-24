package db.serialPort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class ConnectionVerificationResponse {
    /**
     * SYN = 1 接收连接
     */
    private Integer syn;

    /**
     * ACK = 1 对方确认连接
     */
    private Integer ack;

    /**
     * seq 对方确认推送次数
     */
    private Integer seqq;

    /**
     * ack 我的推送次数+1
     */
    private Integer ackk;
}

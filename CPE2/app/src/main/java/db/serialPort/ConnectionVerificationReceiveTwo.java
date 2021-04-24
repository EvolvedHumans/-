package db.serialPort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class ConnectionVerificationReceiveTwo {
    //第二次
    /**
     * ACK = 1， 确认TY-CPE1-V1.0收到
     */
    private Integer ack;

    /**
     * seq 第一次响应推送的ack
     */
    private Integer seqq;

    /**
     * ack 第一次响应推送的seq
     */
    private Integer ackk;
}

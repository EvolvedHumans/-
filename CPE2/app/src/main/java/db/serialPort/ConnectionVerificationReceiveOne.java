package db.serialPort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class ConnectionVerificationReceiveOne {
    //第一次
    /**
     * SYN = 1 发起连接
     */
    private Integer syn;

    /**
     * seq 推送次数
     */
    private Integer seq;



}

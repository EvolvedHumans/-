package db.log;

import org.litepal.crud.LitePalSupport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Data : 注解在类上, 为类提供读写属性, 此外还提供了 equals()、hashCode()、toString() 方法
//@AllArgsConstructor(suppressConstructorProperties = true)报错
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)

public class RunningLog extends LitePalSupport {
    /**
     * 消息类型
     */
    private Integer type = -1;
    /**
     * CPE类型
     */
    private Integer cpeType = -1;
    /**
     * CPE唯一标识
     */
    private String uid ="";
    /**
     * SIM卡ID
     */
    private String simId ="";
    /**
     * 日志内容
     */
    private String data ="";
    /**
     * 状态 true 正常；false 异常
     */
    private Boolean status = false;
    /**
     * 时间戳
     */
    private Long timestamp;

}

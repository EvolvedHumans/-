package db.log;

import org.litepal.crud.LitePalSupport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunningLog extends LitePalSupport {
    /**
     * 消息类型
     */
    private Integer type;
    /**
     * 二级消息类型
     */
    private Integer secondType;
    /**
     * CPE类型
     */
    private Integer cpeType;
    /**
     * CPE唯一标识
     */
    private String uid;
    /**
     * SIM卡ID
     */
    private String simId;
    /**
     * 日志内容
     */
    private String data;
    /**
     * 状态 true 正常；false 异常
     */
    private Boolean status;
    /**
     * 时间戳
     */
    private Long timestamp;
}

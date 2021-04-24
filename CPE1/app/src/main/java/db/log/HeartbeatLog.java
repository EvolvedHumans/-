package db.log;

import org.litepal.crud.LitePalSupport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class HeartbeatLog extends LitePalSupport{
    /**
     * 消息类型
     */
    private Integer type;
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
     * 存储空间
     */
    private Long totalSize;
    /**
     * 可用存储空间
     */
    private Long availableSize;
    /**
     * 可用内存
     */
    private Long availableMemory;

    /**
     * 手机号
     */
    private String phoneNum;

    /**
     * 状态 true 正常；false 异常
     */
    private Boolean status;
    /**
     * 时间戳
     */
    private Long timestamp;

}

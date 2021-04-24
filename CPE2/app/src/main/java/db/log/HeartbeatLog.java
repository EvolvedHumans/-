package db.log;

import org.litepal.crud.LitePalSupport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表对象，表名AndroidState
 * 用于存储手机状态的表
 * json参数为stata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatLog extends LitePalSupport{
    /**
     * 心跳类型
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

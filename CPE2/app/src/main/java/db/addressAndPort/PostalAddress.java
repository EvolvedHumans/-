package db.addressAndPort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年11月10日 11:35
 * @effect : TCP、UDP地址和端口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class PostalAddress {
    /**
     * 地址
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;
}

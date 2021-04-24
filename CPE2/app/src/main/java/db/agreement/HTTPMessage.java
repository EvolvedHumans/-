package db.agreement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2021年01月05日 15:52
 * @effect : HTTP数据分类队列进行储存
 */

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class HTTPMessage {

    private String data;

    private Integer mid;

}

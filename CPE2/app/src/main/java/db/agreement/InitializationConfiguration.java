package db.agreement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年11月06日 14:09
 * @effect :初始化配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class InitializationConfiguration {
    /*
    返回信息状态码
     */
    private String msg;

    /*
    CPE2的UID
     */
    private String uid;

}

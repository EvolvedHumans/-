package db.agreement;

import org.litepal.crud.LitePalSupport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年11月02日 11:16
 * @effect :初始化CPE最外层封装
 */

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)

public class InitCpe<T> extends LitePalSupport {

    /*
    1:rt 正确 ,其他:rt 错误
     */
    private Integer rt;

    /*
    返回信息状态码
     */
    private String msg;

    /*
    状态描述
     */
    private String comments;

    /*
    初始化数据
     */
    private T data;
}

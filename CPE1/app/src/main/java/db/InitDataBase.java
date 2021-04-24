package db;

import org.litepal.LitePal;

import db.log.HeartbeatLog;
import db.log.RunningLog;

/**
 * @Author : YangFan
 * @Date : 2020年11月02日 11:02
 * @effect : 数据库初始化
 */
public class InitDataBase {

    public static void delete(){
        LitePal.deleteAll(RunningLog.class);
        LitePal.deleteAll(HeartbeatLog.class);
    }

}

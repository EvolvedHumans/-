package tool;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class SDCard {

    /**
     * 取得空闲sd卡空间大小
     * @return
     */

    public static long getAvailaleSize(){

        File path = Environment.getExternalStorageDirectory(); //取得sdcard文件路径

        StatFs stat = new StatFs(path.getPath());

        /*获取block的SIZE*/

        long blockSize = stat.getBlockSize();

        /*空闲的Block的数量*/

        long availableBlocks = stat.getAvailableBlocks();

        /* 返回bit大小值*/

        return (availableBlocks * blockSize)/1024 /1024;

        //(availableBlocks * blockSize)/1024      KIB 单位

        //(availableBlocks * blockSize)/1024 /1024  MIB单
    }



    /**
     * SD卡大小
     * @return
     */

    public static long getAllSize(){

        File path = Environment.getExternalStorageDirectory();

        StatFs stat = new StatFs(path.getPath());

        /*获取block的SIZE*/

        long blockSize = stat.getBlockSize();

        /*块数量*/

        long availableBlocks = stat.getBlockCount();

        /* 返回bit大小值*/

        return (availableBlocks * blockSize)/1024 /1024;

    }
}

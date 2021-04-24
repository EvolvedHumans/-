package queue;

import java.util.LinkedList;

/**
 * @author YangFan
 * @date 2020-09-08
 * @description 用于存储消息的缓存队列，将消息上传戴天外网
 *  * 1.初始化配置消息 2.数据消息 3.串口连接校验消息，4.心跳信息，5.日志消息
 */
public class SerialPortQueue {

    private final LinkedList<String> list = new LinkedList<String>();
    private int size = 0;
    private static SerialPortQueue instance;

    /**
     * 初始化方法，确保只存在一个类实例
     */
    public static synchronized SerialPortQueue getInstance(){
        if (instance == null){
            instance = new SerialPortQueue();
        }
        return instance;
    }

    /**
     * 添加一条消息
     */
    public synchronized void put(String ms){
        size++;
        list.addLast(ms);
    }

    /**
     * 使用最大长度限制队列
     */
    public synchronized void maxPut(String ms,Integer maxSize){

        //先添加一条消息进去
        put(ms);

        //然后判断，不能超过最大队列,如果超过，删除第一条消息
        if(size>maxSize){
            get();
        }

    }

    /**
     * 返回并删除第一条消息
     */
    public synchronized String get(){
        if(!empty()){
            size--;
            return list.removeFirst();
        }
        return null;
    }

    /**
     * 判断为空
     */
    public synchronized boolean empty(){
        boolean flag = false;
        if (size == 0){
            flag = true;
        }
        return flag;
    }

    /**
     * 获取长度
     */
    public synchronized int getSize(){
        return size;
    }

    /**
     * 清空队列
     */
    public synchronized void clear(){
        list.clear();
        size = 0;
    }
}

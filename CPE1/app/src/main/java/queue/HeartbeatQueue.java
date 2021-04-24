package queue;

import java.util.LinkedList;

/**
 * @Author : YangFan
 * @Date : 2020年11月04日 15:35
 * @description 用于存储消息的缓存队列，可将mqtt回调类的消息传递给用户
 */
public class HeartbeatQueue {

    private LinkedList<String> list = new LinkedList<String>();
    private int size = 0;
    private static HeartbeatQueue instance;

    /**
     * 初始化方法，确保只存在一个类实例
     */
    public static synchronized HeartbeatQueue getInstance(){
        if (instance == null){
            instance = new HeartbeatQueue();
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

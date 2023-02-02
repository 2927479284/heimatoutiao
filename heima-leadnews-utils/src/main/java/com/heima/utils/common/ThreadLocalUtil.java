package com.heima.utils.common;


public class ThreadLocalUtil {

    private final static ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加用户
     * @param id
     */
    public static void  setUserId(Integer id){
        THREAD_LOCAL.set(id);
    }

    /**
     * 获取用户
     */
    public static Integer getUserId(){
        return THREAD_LOCAL.get();
    }

    /**
     * 清理用户
     */
    public static void clear(){
        THREAD_LOCAL.remove();
    }
}

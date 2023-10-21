package com.purplecloud.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * ClassName: LogUtils
 * Package: com.purplecloud.util
 * Description:
 *
 * @Author HuangXuSen
 * @Create 2023/10/19-15:42
 */
public class LogUtils {
    private static void print(String msg,String level,Object... args){
        if (args != null && args.length > 0)
            msg = String.format(msg.replace("{}", "%s"), args);
        String name =  Thread.currentThread().getName();
        System.out.println(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"))+ "-" + name + ":" + level + ":" + msg);
    }

    public static void info(String msg,Object... args){
        print(msg,"INFO",args);
    }
    public static void error(String msg,Object... args){
        print(msg,"ERROR",args);
    }
}

package com.mtl.util;

import com.mtl.rpc.config.Constant;

/**
 * 说明：字符串工具类
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 12:00
 */
public class StringUtils {
    /**
     * 判断字符非空
     * @param s
     * @return
     */
    public static boolean isEmpty(String s){
        return s==null||"".equals(s.trim());
    }

    /**
     * IP和prot分离
     * @param s
     * @return
     */
    public static String[] analysisIpAndPort(String s){
        if (s==null) return null;
        String[] split = s.split(Constant.IpAndPortSep);
        if (split.length!=2){
            return null;
        }
        return split;
    }
}

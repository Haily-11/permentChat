package com.shanyangcode.realtimeservice.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author haily
 */
public class SnowflakeDynamicUtil {

    //这里在yml中随便配置了两个数据
    @Value("${snowflake.workerId}")
    private static  long workerId;

    @Value("${snowflake.datacenterId}")
    private static  long dataCenterId;

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(workerId,dataCenterId);

    /**
     *
     * @return生成雪花ID
     */
    public static long nextId(){
        return SNOWFLAKE.nextId();
    }
}

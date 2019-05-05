package com.mtl.rpc;

import com.mtl.rpc.config.Constant;
import com.mtl.rpc.config.NettyConfig;
import com.mtl.rpc.config.RpcClientConfiguration;
import com.mtl.rpc.exception.AppException;
import com.mtl.rpc.message.Request;
import com.mtl.rpc.proxy.RpcInterfaceProxyFactroyBean;
import com.mtl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 说明：随机选择器
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 16:24
 */
public class RandomServerSelector extends AbstractSelector {
    private SecureRandom random=new SecureRandom();
    @Override
    public ServerInfo doSelect(List<ServerInfo> serverList, Request request) {
        int i = random.nextInt(serverList.size());
        return serverList.get(i);
    }
}

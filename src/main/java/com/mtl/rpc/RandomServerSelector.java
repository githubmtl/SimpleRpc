package com.mtl.rpc;

import java.security.SecureRandom;
import java.util.List;

/**
 * 说明：随机选择器
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 16:24
 */
public class RandomServerSelector implements ServerSelector {
    private SecureRandom random=new SecureRandom();
    @Override
    public ServerInfo select(List<ServerInfo> serverList) {
        if (serverList==null||serverList.size()==0)
            return null;
        int i = random.nextInt(serverList.size());
        return serverList.get(i);
    }
}

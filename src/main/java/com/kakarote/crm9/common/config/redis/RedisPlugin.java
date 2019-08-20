package com.kakarote.crm9.common.config.redis;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.IPlugin;

import java.io.File;
import java.nio.charset.Charset;

/**
 * RedisPlugin
 */
public class RedisPlugin implements IPlugin {

    public RedisPlugin(){

    }

    @Override
    public boolean start() {
        JSONObject readJSONObject= JSONUtil.readJSONObject(new File(PathKit.getRootClassPath()+"/config/redis.json"), Charset.defaultCharset());
        RedisManager.me()._init(readJSONObject);
        return true;
    }

    @Override
    public boolean stop() {
        RedisManager.getRedis().destroy();
        return true;
    }
}

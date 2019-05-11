package com.kakarote.crm9.common.config.cache;

import com.jfinal.plugin.activerecord.cache.ICache;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class RedisCache implements ICache {
    @Override
    public <T> T get(String s, Object o) {
        Cache cache= Redis.use(s);
        if(cache==null){
            cache=Redis.use();
        }
        return cache.get(o);
    }

    @Override
    public void put(String s, Object o, Object o1) {
        Cache cache= Redis.use(s);
        if(cache==null){
            cache=Redis.use();
        }
        cache.setex(o,1800,o1);
    }

    @Override
    public void remove(String s, Object o) {
        Cache cache= Redis.use(s);
        if(cache==null){
            cache=Redis.use();
        }
        cache.del(o);
    }

    @Override
    public void removeAll(String s) {
        Cache cache= Redis.use(s);
        if(cache==null){
            cache=Redis.use();
        }
        cache.flushAll();
    }
}

package com.kakarote.crm9.common.config.cache;

import com.jfinal.plugin.activerecord.cache.ICache;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisCache implements ICache {
    @Override
    public <T> T get(String cacheName, Object key) {
        Cache cache= Redis.use(cacheName);
        if(cache==null){
            return null;
        }
        return cache.get(key);
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        Cache cache= Redis.use(cacheName);
        if(cache==null){
            return;
        }
        cache.setex(key,1800,value);
    }

    @Override
    public void remove(String cacheName, Object key) {
        Cache cache= Redis.use(cacheName);
        if(cache==null){
            return;
        }
        cache.del(key);
    }

    @Override
    public void removeAll(String cacheName) {
        Cache cache= Redis.use(cacheName);
        if(cache==null){
            return;
        }
        try {
            cache.flushDB();
        }catch (JedisDataException e){
            //TODO flushDB命令可能被禁用,keys命令在当数据规模较大时，会严重影响Redis性能
            try {
                cache.del(cache.keys("*").toArray());
            }catch (JedisDataException exception){
                //TODO keys同样被禁用
                throw new RuntimeException(exception);
            }
        }

    }
}

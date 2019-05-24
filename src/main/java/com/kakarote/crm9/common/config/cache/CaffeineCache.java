package com.kakarote.crm9.common.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jfinal.plugin.activerecord.cache.ICache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class CaffeineCache implements ICache {
    private static ConcurrentHashMap<String, Cache<Object, Object>> cacheConcurrentHashMap = new ConcurrentHashMap<>(8, 0.75f);

    public static final CaffeineCache ME = new CaffeineCache();

    private static final String DEFAULT_KEY="default";

    private CaffeineCache() {
        cacheConcurrentHashMap.put(DEFAULT_KEY,Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, Object key) {
        Cache<Object, Object> cache = cacheConcurrentHashMap.get(cacheName);
        if (cache != null) {
            return (T) cache.getIfPresent(key);
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return get(DEFAULT_KEY,key);
    }
    @Override
    public void put(String cacheName, Object key, Object value) {
        Cache<Object, Object> cache = cacheConcurrentHashMap.get(cacheName);
        if (cache == null) {
            cache = Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build();
            cacheConcurrentHashMap.put(cacheName, cache);
        }
        cache.put(key, value);
    }
    public void put(Object key, Object value) {
        put(DEFAULT_KEY,key,value);
    }
    @Override
    public void remove(String cacheName, Object key) {
        Cache<Object, Object> cache = cacheConcurrentHashMap.get(cacheName);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    public void remove(Object key){
        remove(DEFAULT_KEY,key);
    }

    @Override
    public void removeAll(String cacheName) {
        Cache<Object, Object> cache = cacheConcurrentHashMap.get(cacheName);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

}

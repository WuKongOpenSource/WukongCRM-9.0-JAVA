package com.kakarote.crm9.common.config.redis;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.redis.IKeyNamingPolicy;
import com.jfinal.plugin.redis.serializer.FstSerializer;
import com.kakarote.crm9.common.constant.BaseConstant;
import redis.clients.jedis.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis管理
 */
public class RedisManager {
    private static RedisManager redisManager=new RedisManager();
    private Redis redis;
    private Integer maxTotal = 1000;
    private Integer maxIdle = 200;
    private Long maxWaitMillis = 2000L;
    private Boolean testOnBorrow = true;

    private RedisManager(){}

    public static RedisManager me(){
        return redisManager;
    }
    public static Redis getRedis(){
        return RedisManager.me().getRedisConfig();
    }

    public Redis getRedisConfig(){
        if(this.redis!=null){
            return redis;
        }
        JSONObject readJSONObject= JSONUtil.readJSONObject(new File(PathKit.getRootClassPath()+"/config/redis.json"), Charset.defaultCharset());
        return _init(readJSONObject);
    }

    public synchronized Redis _init(JSONObject jsonObject){
        if(this.redis!=null){
            return redis;
        }
        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        JSONArray jsonArray=jsonObject.getJSONArray("host");
        if(jsonArray.size()==0){
            throw new RuntimeException("redis信息配置错误");
        }
        String password=jsonObject.getStr("password");
        //1 单机版redis
        if(jsonObject.getInt("type").equals(1)){
            String host=jsonArray.getStr(0).split(":")[0];
            Integer port=jsonArray.getStr(0).split(":").length>1?Integer.valueOf(jsonArray.getStr(0).split(":")[1]):6379;
            JedisPool jedisPool;
            if(StrUtil.isNotEmpty(password)){
                jedisPool=new JedisPool(jedisPoolConfig,host,port,2000,password,jsonObject.getInt("database",0));
            }else {
                jedisPool=new JedisPool(jedisPoolConfig,host,port,2000);
            }
            redis=new JedisImpl(jedisPool,FstSerializer.me, IKeyNamingPolicy.defaultKeyNamingPolicy);
        }
        // RedisCluster
        else if(jsonObject.getInt("type").equals(2)){
            Set<HostAndPort> hostAndPorts=new HashSet<>();
            jsonArray.forEach(obj->{
                String host=obj.toString().split(":")[0];
                Integer port=obj.toString().split(":").length>1?Integer.valueOf(obj.toString().split(":")[1]):6379;
                HostAndPort hostAndPort=new HostAndPort(host,port);
                hostAndPorts.add(hostAndPort);
            });
            JedisCluster jedisCluster=new JedisCluster(hostAndPorts,2000,2000,2000,password,jedisPoolConfig);
            redis=new JedisClusterImpl(jedisCluster,FstSerializer.me, IKeyNamingPolicy.defaultKeyNamingPolicy);
        }
        // RedisSentinel
        else if(jsonObject.getInt("type").equals(3)){
            Set<String> stringHashSet=new HashSet<>();
            jsonArray.forEach(obj->stringHashSet.add(obj.toString()));
            JedisSentinelPool jedisSentinelPool;
            if(StrUtil.isNotEmpty(password)){
                jedisSentinelPool=new JedisSentinelPool(jsonObject.getStr("cacheName"),stringHashSet,jedisPoolConfig,2000,password,jsonObject.getInt("database",0));
            }else {
                jedisSentinelPool=new JedisSentinelPool(jsonObject.getStr("cacheName"),stringHashSet,jedisPoolConfig);
            }
            redis=new JedisSentinelImpl(jedisSentinelPool,FstSerializer.me, IKeyNamingPolicy.defaultKeyNamingPolicy);
        }

        return redis;
    }

    private String decrypt(String str){
        RSA rsa=SecureUtil.rsa("PRIVATE_KEY","PUBILC_KEY");
        return rsa.decryptFromBcdToStr(str,KeyType.PrivateKey, CharsetUtil.CHARSET_UTF_8);
    }
    private String encrypt(String key){
        RSA rsa=SecureUtil.rsa("PRIVATE_KEY","PUBILC_KEY");
        return rsa.encryptHex(key,CharsetUtil.CHARSET_UTF_8,KeyType.PublicKey);
    }

}

package com.ses.sesCache;

import com.ses.util.serialization.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * 使用redis实现的cache提供者
 *
 * @author zhangyanqi
 * @since 1.0 2017/12/16
 */
public class RedisCacheProvider implements CacheProvider {


    public RedisCacheProvider(JedisPool pool, SerializationUtil util) {
        this.pool = pool;
        this.serializationUtil = util;
    }

    public RedisCacheProvider(JedisPool pool, SerializationUtil util, String characterEncoding) {
        this.pool = pool;
        this.serializationUtil = util;
        this.characterEncoding = characterEncoding;
    }

    private static Logger logger = LoggerFactory.getLogger(RedisCacheProvider.class);

    /**
     * redis连接池，在运行时依赖注入
     * <p>
     * example:
     * 初始化连接池:
     * JedisPoolConfig config = new JedisPoolConfig();
     * config.setMaxTotal(100);//最大连接数
     * config.setMaxIdle(5);//最大空闲连接
     * pool = new JedisPool(config,"127.0.0.1",6379,超时时长);//创建redis连接池
     * <p>
     * 使用连接池:
     * Jedis jedis = pool.getRecource();
     * String value = jedis.get(key);
     * jedis.close();//使用之后记得关闭连接
     */
    private JedisPool pool;

    /**
     * 序列化工具
     */
    private SerializationUtil serializationUtil;

    /**
     * 在redis中无法存储byte，可以存储string类型的数据，在使用缓存相关方法前，需要确定使用什么字符串编码的方式,默认使用UTF-8
     */
    private String characterEncoding = "UTF-8";


    /**
     * 从缓存容器中获得单个缓存对象
     *
     * @param key   缓存的键
     * @param clazz 缓存对象的类型
     * @return 被缓存的对象 如果为null则未命中
     */
    @Override
    public <T> T get(String key, Class<T> clazz) {
        Jedis resource = pool.getResource();
        String s = resource.get(key);
        if (s.isEmpty()) {
            logger.info("查询缓存{},未命中", key);
            return null;
        }
        byte[] bytes = null;
        try {
            bytes = s.getBytes(characterEncoding);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
            return null;
        }
        logger.info("查询缓存{},命中", key);
        return serializationUtil.deserialize(bytes, clazz);
    }

    /**
     * 从缓存容器中批量获得缓存对象,对象类型只能是同一种
     * 使用pipeline
     *
     * @param keys  缓存的键
     * @param clazz 缓存对象的类型
     * @return 被缓存的对象
     */
    @Override
    public <T> List<T> get(List<String> keys, Class<T> clazz) {
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }
        List returnList = new ArrayList<T>(20);
        Jedis resource = pool.getResource();
        Pipeline pipelined = resource.pipelined();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            pipelined.get(next);
        }
        List<Object> objects = pipelined.syncAndReturnAll();
        Iterator<Object> returnObjects = objects.iterator();
        while (returnObjects.hasNext()) {
            Object next = returnObjects.next();
            try {
                returnList.add(serializationUtil.deserialize(((String) next).getBytes(characterEncoding), clazz));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage());
                return new ArrayList<>();
            }
        }
        return returnList;
    }

    /**
     * 设置缓存的对象
     *
     * @param key     键
     * @param toStore 需要存储的对象
     * @param ttl     过期时间 -1 永不过期 单位是秒
     * @return 是否设置成功
     */
    @Override
    public boolean set(String key, Object toStore, int ttl) {
        byte[] serializationDate = serializationUtil.serialize(toStore);
        String s = null;
        try {
            s = new String(serializationDate, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
            return false;
        }
        Jedis resource = pool.getResource();
        resource.set(key, s);
        if (ttl == -1) {
            //do nothing
        } else {
            resource.expire(key, ttl);
        }
        logger.info("设置缓存{}", key);
        resource.close();
        return true;
    }

    /**
     * 批量设置缓存的对象
     * 使用pipeline
     *
     * @param params
     * @return 是否设置成功
     */
    @Override
    public boolean set(Map<String, Object> params) {
        if (params.size() < 1) {
            return true;
        }
        Jedis resource = pool.getResource();
        Pipeline pipelined = resource.pipelined();
        Set<Map.Entry<String, Object>> entries = params.entrySet();
        Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> next = iterator.next();
            String key = next.getKey();
            Object value = next.getValue();
            String s = null;
            try {
                s = new String(serializationUtil.serialize(value), characterEncoding);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return false;
            }
            pipelined.set(key, s);
        }
        pipelined.sync();
        resource.close();
        return true;
    }

    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     */
    @Override
    public void delete(List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }
        Jedis resource = pool.getResource();
        Pipeline pipelined = resource.pipelined();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            pipelined.del(next);
        }
        pipelined.sync();
        resource.close();
    }

    /**
     * 单个删除缓存
     *
     * @param key 单个键
     */
    @Override
    public void delete(String key) {
        logger.info("删除缓存{}", key);
        Jedis resource = pool.getResource();
        resource.del(key);
        resource.close();
    }


    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public SerializationUtil getSerializationUtil() {
        return serializationUtil;
    }

    public void setSerializationUtil(SerializationUtil serializationUtil) {
        this.serializationUtil = serializationUtil;
    }

    public JedisPool getPool() {
        return pool;
    }

    public void setPool(JedisPool pool) {
        this.pool = pool;
    }

    /**
     * builder模式来构造缓存提供者
     * 目前用处不不大
     */
    public static class RedisCacheProviderBuilder {

        private JedisPool pool;

        private SerializationUtil serializationUtil;

        private String characterEncoding = "UTF-8";

        public RedisCacheProviderBuilder setPool(JedisPool pool) {
            this.pool = pool;
            return this;
        }

        public RedisCacheProviderBuilder setSerializationUtil(SerializationUtil util) {
            this.serializationUtil = util;
            return this;
        }

        public RedisCacheProviderBuilder setCharacterEncoding(String encoding) {
            this.characterEncoding = encoding;
            return this;
        }

        public RedisCacheProvider build() {
            return new RedisCacheProvider(pool, serializationUtil, characterEncoding);
        }
    }
}

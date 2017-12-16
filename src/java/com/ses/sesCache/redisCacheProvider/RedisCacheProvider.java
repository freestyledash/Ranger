package com.ses.sesCache.redisCacheProvider;

import com.ses.sesCache.CacheProvider;
import com.ses.util.serialization.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;

/**
 * 使用redis实现的cache提供者
 *
 * @author zhangyanqi
 * @since 1.0 2017/12/16
 */
public class RedisCacheProvider implements CacheProvider {

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
     * 从缓存容器中获得单个缓存对象
     *
     * @param key   缓存的键
     * @param clazz 缓存对象的类型
     * @return 被缓存的对象
     */
    @Override
    public <T> T get(String key, Class<T> clazz) {
        return null;
    }

    /**
     * 从缓存容器中批量获得缓存对象
     *
     * @param keys  缓存的键
     * @param clazz 缓存对象的类型
     * @return 被缓存的对象
     */
    @Override
    public <T> T get(List<String> keys, Class<T> clazz) {
        return null;
    }

    /**
     * 设置缓存的对象
     *
     * @param key               键
     * @param serializationDate 序列化之后的值
     * @return 是否设置成功
     */
    @Override
    public boolean set(String key, byte[] serializationDate) {
        return false;
    }

    /**
     * 批量设置缓存的对象
     *
     * @param params
     * @return 是否设置成功
     */
    @Override
    public boolean set(Map<String, byte[]> params) {
        return false;
    }

    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     */
    @Override
    public void delete(List<String> keys) {

    }

    /**
     * 单个删除缓存
     *
     * @param key 单个键
     */
    @Override
    public void delete(String key) {

    }
}

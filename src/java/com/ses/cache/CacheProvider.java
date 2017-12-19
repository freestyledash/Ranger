package com.ses.cache;

import java.util.List;
import java.util.Map;

/**
 * cache服务提供者
 * 实现类使用的缓存容器应该为使用k-v方式存储数据的数据库
 *
 * @author zhangyanqi
 * @since 1.0 2017/12/16
 */
public interface CacheProvider {

    /**
     * 从缓存容器中获得单个缓存对象
     *
     * @param key   缓存的键
     * @param clazz 缓存对象的类型
     * @param <T>   缓存的类型
     * @return 被缓存的对象
     */
    <T> T getCache(String key, Class<T> clazz);


    /**
     * 从缓存容器中批量获得缓存对象，对象类型只能是同一种
     *
     * @param keys  缓存的键
     * @param clazz 缓存对象的类型
     * @param <T>   缓存的类型
     * @return 被缓存的对象
     */
    <T> List<T> getCache(List<String> keys, Class<T> clazz);

    /**
     * 设置缓存的对象
     *
     * @param key     键
     * @param toStore 需要存储的对象
     * @param ttl     过期时间 -1 永不过期 ，单位是秒
     * @return 是否设置成功
     */
    boolean setCache(String key, Object toStore, int ttl);


    /**
     * 批量设置缓存的对象
     *
     * @param params 需要设置的对象
     * @param ttl 对象存活时间
     * @return 是否设置成功
     */
    boolean setCache(Map<String, Object> params, int ttl);


    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     */
    void deleteCache(List<String> keys);


    /**
     * 单个删除缓存
     *
     * @param key 单个键
     */
    void deleteCache(String key);


}

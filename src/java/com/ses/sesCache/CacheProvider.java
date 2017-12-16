package com.ses.sesCache;

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
    <T> T get(String key, Class<T> clazz);


    /**
     * 从缓存容器中批量获得缓存对象
     *
     * @param keys  缓存的键
     * @param clazz 缓存对象的类型
     * @param <T>   缓存的类型
     * @return 被缓存的对象
     */
    <T> T get(List<String> keys, Class<T> clazz);

    /**
     * 设置缓存的对象
     *
     * @param key               键
     * @param serializationDate 序列化之后的值
     * @return 是否设置成功
     */
    boolean set(String key, byte[] serializationDate);


    /**
     * 批量设置缓存的对象
     *
     * @return 是否设置成功
     */
    boolean set(Map<String, byte[]> params);


    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     */
    void delete(List<String> keys);


    /**
     * 单个删除缓存
     *
     * @param key 单个键
     */
    void delete(String key);


}

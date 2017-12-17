# SESCache

## 使用方式:
1.创建provider对象，改对象推荐设置为全局对象，可以使用容器组装provider对象，比如spring框架
```$xslt
        //init jdies pool
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);//最大连接数
        config.setMaxIdle(5);//最大空闲连接
        JedisPool pool = new JedisPool(config, "127.0.0.1", 6379, 1000);//创建redis连接池

        //init serializationUtil
        ProtostuffSerializationUtil util = new ProtostuffSerializationUtil();

        //init provider，combine pool and uitl
        RedisCacheProvider provider = new RedisCacheProvider();
        provider.setPool(pool);
        provider.setSerializationUtil(util);
```
## provider对象提供以下方法:
```$xslt
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
     * 从缓存容器中批量获得缓存对象，对象类型只能是同一种
     *
     * @param keys  缓存的键
     * @param clazz 缓存对象的类型
     * @param <T>   缓存的类型
     * @return 被缓存的对象
     */
    <T> List<T> get(List<String> keys, Class<T> clazz);

    /**
     * 设置缓存的对象
     *
     * @param key     键
     * @param toStore 需要存储的对象
     * @return 是否设置成功
     */
    boolean set(String key, Object toStore);


    /**
     * 批量设置缓存的对象
     *
     * @return 是否设置成功
     */
    boolean set(Map<String, Object> params);


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

```
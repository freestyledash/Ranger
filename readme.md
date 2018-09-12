# Ranger
轻量级缓存中间件，目前支持redis作为缓存介质来储存数据

## 使用方式:
1.创建provider对象，改对象推荐设置为全局对象，推荐使用IOC容器组装provider对象
```
        //init jdies pool
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);//最大连接数
        config.setMaxIdle(5);//最大空闲连接
        JedisPool pool = new JedisPool(config, "127.0.0.1", 6379, 1000);//创建redis连接池

        //init serializationUtil
        ProtostuffSerializationUtil util = new ProtostuffSerializationUtil();

        //init providerFactory，combine pool and uitl
        CacheProviderFactory c = new CacheProviderFactory(pool, util);
        
        //getProvider
        CacheProvider provider = c.getProvider();
```
或者使用链式表达式创建对象
```
    new CacheProviderFactory.Builder().setLock(null).setPool(null).setSerializationUtil().build().getProvider()
```

2 provider对象提供以下方法:
```
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

```
3 缓存在项目中的使用
设计目标是让开发者在service层中进行自定义的调用(cache aside)

4 缓存设计思想
通过读写锁防止缓存雪崩,但是可能在一些写操作过多的场景下会出现性能差的情况




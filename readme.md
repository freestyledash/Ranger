# SESCache

使用方式:
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
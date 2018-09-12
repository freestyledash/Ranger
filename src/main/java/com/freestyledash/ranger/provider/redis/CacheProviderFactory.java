package com.freestyledash.ranger.provider.redis;

import com.freestyledash.ranger.provider.CacheProvider;
import com.freestyledash.ranger.util.serialization.SerializationUtil;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 根据策略创建RedisCache的工厂
 * 根据配置
 * 创建只有单个jvm访问数据库场景的CacheProvider
 * 创建存在jvm访问数据库场景的CacheProvider
 *
 * @author zhangyanqi
 * @since 1.0 2017/12/22
 */
public class CacheProviderFactory {

    private final static String READMETHOD = "get";

    /**
     * 提供缓存服务和核心
     */
    private final CacheProviderCore cacheProviderCore;

    /**
     * 真正对外提供服务的对象
     */
    private CacheProvider proxyProvider;

    /**
     * 读写锁,用于防止出现缓存雪崩情况
     * 默认使用ReentrantReadWriteLock
     * 在单个jvm使用数据库的情况下，可以使用默认锁
     * 当在集群情况下推荐使用一种可以协调多个jvm的锁
     */
    private final ReadWriteLock lock;

    public CacheProviderFactory(JedisPool pool, SerializationUtil util, final ReadWriteLock lock) {
        this.lock = lock;
        if (lock == null) {
            throw new IllegalArgumentException("ReadWriteLock对象不能为空");
        }
        cacheProviderCore = new CacheProviderCore(pool, util);
        proxyProvider = (CacheProvider) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{CacheProvider.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;
                        //读取操作上读锁，写入操作上写锁
                        //如果是Object的方法，放行
                        if (Object.class.equals(method.getDeclaringClass())) {
                            return method.invoke(this, args);
                        }
                        //如果是读,则上读锁
                        if (method.getName().contains(READMETHOD)) {
                            try {
                                lock.readLock().lock();
                            } catch (Exception e) {
                                throw new RuntimeException("上锁失败");
                            }
                            try {
                                result = method.invoke(cacheProviderCore, args);
                            } catch (Exception e) {
                                throw new RuntimeException(method.getName() + "执行失败");
                            } finally {
                                lock.readLock().unlock();
                            }
                            return result;
                        } else {
                            //插入和删除操作，写锁
                            try {
                                lock.writeLock().lock();
                            } catch (Exception e) {
                                throw new RuntimeException("上锁失败");
                            }
                            try {
                                result = method.invoke(cacheProviderCore, args);
                            } catch (Exception e) {
                                throw new RuntimeException(method.getName() + "执行失败");
                            } finally {
                                lock.writeLock().unlock();
                            }
                            return result;
                        }
                    }
                }
        );
    }

    public CacheProviderFactory(JedisPool pool, SerializationUtil util) {
        this(pool, util, new ReentrantReadWriteLock());
    }


    /**
     * 获得CacheProvider
     *
     * @return CacheProvider
     */
    public CacheProvider getProvider() {
        if (proxyProvider == null) {
            throw new IllegalStateException("proxyProvider 未初始化");
        }
        return proxyProvider;
    }


    public static class Builder {

        private JedisPool pool;

        private SerializationUtil serializationUtil;

        private ReadWriteLock lock;

        public CacheProviderFactory.Builder setPool(JedisPool pool) {
            this.pool = pool;
            return this;
        }

        public CacheProviderFactory.Builder setSerializationUtil(SerializationUtil util) {
            this.serializationUtil = util;
            return this;
        }

        public CacheProviderFactory.Builder setLock(ReadWriteLock lock) {
            this.lock = lock;
            return this;
        }

        public CacheProviderFactory build() {
            if (pool == null || serializationUtil == null) {
                throw new IllegalStateException("pool和serializationUtil未初始化");
            }
            if (lock == null) {
                return new CacheProviderFactory(pool, serializationUtil);
            } else {
                return new CacheProviderFactory(pool, serializationUtil, lock);
            }
        }
    }
}

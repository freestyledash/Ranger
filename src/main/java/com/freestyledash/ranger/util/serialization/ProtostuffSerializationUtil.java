package com.freestyledash.ranger.util.serialization;

import io.protostuff.GraphIOUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * protustuff工具实现序列化功能
 *
 * @author zhangyanqi
 * @since 1.0 2017/12/16
 */
public final class ProtostuffSerializationUtil implements SerializationUtil {

    private final Map<Class<?>, Schema<?>> cachedSchemas;

    private final Objenesis objenesis;

    {
        cachedSchemas = new ConcurrentHashMap<Class<?>, Schema<?>>();
        objenesis = new ObjenesisStd(true);
    }

    /**
     * 将对象<code>message</code>序列化为字节数组
     *
     * @param message 要序列化的对象
     * @return 序列化后的字节数组
     */
    @Override
    @SuppressWarnings("unchecked")
    public byte[] serialize(Object message) {
        Wrapper wrapper = new Wrapper(message);
        Class clazz = wrapper.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate();
        try {
            Schema schema = getSchema(clazz);
            return GraphIOUtil.toByteArray(wrapper, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 将字节数组<code>data</code>反序列化为对象
     *
     * @param data  字节数组
     * @param clazz 对象类型的Class对象
     * @param <T>   对象的类型参数
     * @return 反序列化后的对象
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Wrapper message = objenesis.newInstance(Wrapper.class);
        Schema<Wrapper> schema = getSchema(Wrapper.class);
        GraphIOUtil.mergeFrom(data, message, schema);
        return (T) message.realObject;
    }

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) cachedSchemas.get(clazz);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(clazz);
            cachedSchemas.put(clazz, schema);
        }
        return schema;
    }


    /**
     * 为了解决一些类无法被正确的序列化，但是作为一个类的属性就可以正确的序列化，于是将所有的类都装进warpper中
     * 改wrapper对象对外界不可见
     *
     * @param <T> 对象类型
     */
    private static class Wrapper<T> {

        /**
         * 用户存储的对象
         */
        public T realObject;

        public Wrapper(T realObject) {
            this.realObject = realObject;
        }
    }

}
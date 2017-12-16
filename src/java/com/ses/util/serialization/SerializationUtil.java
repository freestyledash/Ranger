package com.ses.util.serialization;

/**
 * 序列化工具的接口
 *
 * @author zhangyanqi
 * @since 1.0 2017/12/16
 */
public interface SerializationUtil {

    /**
     * @param toSerialize 将要被序列化的对象
     * @return 序列化之后的数组
     */
    byte[] serialize(Object toSerialize);

    /**
     * 序列化
     *
     * @param data  被反序列化的数组
     * @param clazz 目标类型对象
     * @param <T>   目标类型
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> clazz);

}

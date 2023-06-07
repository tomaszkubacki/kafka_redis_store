package org.tk.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonSerde<T> implements Serializer<T>, Deserializer<T>, Serde<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    Class<T> clazz;

    public JsonSerde(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @SneakyThrows
    @Override
    public byte[] serialize(String topic, T data) {
        try {
            if (data == null) {
                System.out.println("Null received at serializing");
                return null;
            }
            System.out.println("Serializing...");
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new Exception("Error when serializing Message to byte[]");
        }
    }

    @SneakyThrows
    @Override
    public T deserialize(String topic, byte[] data) {
        return data != null && data.length > 0 ? objectMapper.readValue(data, this.clazz) : null;
    }


    @Override
    public void close() {
    }

    @Override
    public Serializer<T> serializer() {
        return this;
    }

    @Override
    public Deserializer<T> deserializer() {
        return this;
    }
}
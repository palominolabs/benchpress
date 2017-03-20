package com.palominolabs.benchpress.curator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;

import java.io.ByteArrayOutputStream;

final class JacksonInstanceSerializer<T> implements InstanceSerializer<T> {
    private final TypeReference<ServiceInstance<T>> typeRef;
    private final ObjectWriter objectWriter;
    private final ObjectReader objectReader;

    JacksonInstanceSerializer(ObjectReader objectReader, ObjectWriter objectWriter,
                              TypeReference<ServiceInstance<T>> typeRef) {
        this.objectReader = objectReader;
        this.objectWriter = objectWriter;
        this.typeRef = typeRef;
    }

    @Override
    public ServiceInstance<T> deserialize(byte[] bytes) throws Exception {
        return objectReader.forType(typeRef).readValue(bytes);
    }

    @Override
    public byte[] serialize(ServiceInstance<T> serviceInstance) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        objectWriter.writeValue(out, serviceInstance);
        return out.toByteArray();
    }
}

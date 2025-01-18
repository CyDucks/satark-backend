package org.cyducks.satark.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cyducks.satark.model.Coordinate;
import org.cyducks.satark.model.RoutingNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RoutingTableService {
    @Getter
    private final Map<Coordinate, List<RoutingNode>> routingTable = new ConcurrentHashMap<>();

    @Value("${routing.table.path}")
    private String routingTablePath;

    private final ObjectMapper objectMapper;

    public RoutingTableService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        SimpleModule module = new SimpleModule();
        module.addKeyDeserializer(Coordinate.class, new CoordinateDeserializer() {});
        objectMapper.registerModule(module);
    }

    @PostConstruct
    public void init() throws IOException {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(routingTablePath, "r")) {
            FileChannel channel = randomAccessFile.getChannel();

            MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            byte[] bytes = new byte[(int) channel.size()];
            mappedByteBuffer.get(bytes);

            Map<Coordinate, List<RoutingNode>> loadedTable = objectMapper.readValue(
                    bytes,
                    new TypeReference<Map<Coordinate, List<RoutingNode>>>() {}
            );
            routingTable.putAll(loadedTable);
        }
    }

    public static class CoordinateDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String s, DeserializationContext deserializationContext) throws IOException {
            return new Coordinate(s);
        }
    }

}

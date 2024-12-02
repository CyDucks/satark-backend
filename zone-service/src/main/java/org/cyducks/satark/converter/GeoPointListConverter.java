package org.cyducks.satark.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.cyducks.satark.data.GeoPoint;

import java.util.List;

@Converter
public class GeoPointListConverter implements AttributeConverter<List<GeoPoint>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<GeoPoint> geoPoints) {
        try {
            return mapper.writeValueAsString(geoPoints);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting points to JSON");
        }
    }

    @Override
    public List<GeoPoint> convertToEntityAttribute(String s) {
        try {
            return mapper.readValue(s, new TypeReference<List<GeoPoint>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to points");
        }
    }
}

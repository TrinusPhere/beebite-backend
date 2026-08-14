package com.fidecode.biteflow.infrastructure.persistence.panache.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Converte List<String> ↔ String[] para coluna text[] do PostgreSQL.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String[]> {

    @Override
    public String[] convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) return new String[0];
        return attribute.toArray(new String[0]);
    }

    @Override
    public List<String> convertToEntityAttribute(String[] dbData) {
        if (dbData == null) return List.of();
        return Arrays.asList(dbData);
    }
}

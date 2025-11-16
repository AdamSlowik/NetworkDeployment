package org.example.config.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import inet.ipaddr.mac.MACAddress;

import java.io.IOException;

public class MacSerializer extends JsonSerializer<MACAddress> {

    @Override
    public void serialize(
            MACAddress value,
            JsonGenerator gen,
            SerializerProvider serializers
    ) throws IOException {
        if (value != null) {
            gen.writeString(value.toColonDelimitedString());
        } else {
            gen.writeNull();
        }
    }

}
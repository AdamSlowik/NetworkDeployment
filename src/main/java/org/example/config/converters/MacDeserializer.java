package org.example.config.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import inet.ipaddr.mac.MACAddress;

import java.io.IOException;

import static org.example.util.StringToMacConverter.convertToMac;

public class MacDeserializer extends StdDeserializer<MACAddress> {
    public MacDeserializer() {
        this(null);
    }

    protected MacDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public MACAddress deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        var address = node.asText();
        return convertToMac(address);
    }
}


package org.example.config.converters;

import inet.ipaddr.mac.MACAddress;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.value.StringValue;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.HashSet;
import java.util.Set;

import static org.example.util.StringToMacConverter.convertToMac;

public class MacAddressPropertyConverter implements GenericConverter {


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> convertiblePairs = new HashSet<>();
        convertiblePairs.add(new ConvertiblePair(MACAddress.class, Value.class));
        convertiblePairs.add(new ConvertiblePair(Value.class, MACAddress.class));
        return convertiblePairs;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (MACAddress.class.isAssignableFrom(sourceType.getType())) {
            return Values.value(((MACAddress) source).toColonDelimitedString());
        } else {
            return convertToMac(((StringValue) source).asString());
        }
    }
}

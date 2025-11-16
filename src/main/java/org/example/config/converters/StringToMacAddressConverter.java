package org.example.config.converters;

import inet.ipaddr.mac.MACAddress;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static org.example.util.StringToMacConverter.convertToMac;

@Component
public class StringToMacAddressConverter implements Converter<String, MACAddress> {

    @Override
    public MACAddress convert(String source) {
        return convertToMac(source);
    }
}
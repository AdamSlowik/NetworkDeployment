package org.example.util;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.MACAddressString;
import inet.ipaddr.mac.MACAddress;
import org.example.exception.MalformedMacAddress;

public class StringToMacConverter {
    public static MACAddress convertToMac(String stringAddress) {
        try {
            return new MACAddressString(stringAddress).toAddress();
        } catch (AddressStringException e) {
            throw new MalformedMacAddress("Malformed MAC address: %s".formatted(stringAddress));
        }
    }
}

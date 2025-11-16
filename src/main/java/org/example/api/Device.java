package org.example.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import inet.ipaddr.mac.MACAddress;
import jakarta.validation.constraints.NotNull;
import org.example.config.converters.MacSerializer;

import java.util.Optional;

public record Device(
        @NotNull
        @JsonSerialize(using = MacSerializer.class)
        MACAddress macAddress,
        @NotNull
        DeviceType deviceType,
        @NotNull
        @JsonSerialize(contentUsing = MacSerializer.class)
        Optional<MACAddress> uplinkMacAddress
) {
}

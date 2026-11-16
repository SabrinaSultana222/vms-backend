package com.vms.backend.config;

import com.vms.backend.entity.VehicleStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToVehicleStatusConverter implements Converter<String, VehicleStatus> {

    @Override
    public VehicleStatus convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        return VehicleStatus.fromString(source);
    }
}

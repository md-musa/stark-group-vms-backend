package com.musa.stark_group_vms.exception;

public class VehicleInUseException extends RuntimeException {
    public VehicleInUseException(String message) {
        super(message);
    }
}

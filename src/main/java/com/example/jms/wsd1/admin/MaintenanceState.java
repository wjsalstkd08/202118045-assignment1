package com.example.jms.wsd1.admin;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class MaintenanceState {

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public boolean isEnabled() { return enabled.get(); }

    public void setEnabled(boolean value) { enabled.set(value); }
}

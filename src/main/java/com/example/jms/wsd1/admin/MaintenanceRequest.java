package com.example.jms.wsd1.admin;

import jakarta.validation.constraints.NotNull;

public class MaintenanceRequest {

    @NotNull(message = "enabled 값이 필요합니다.")
    private Boolean enabled;

    // getter / setter
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}

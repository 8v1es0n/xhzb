package com.xhzb.nursing.domain;

import com.xhzb.common.core.domain.BaseEntity;

import java.math.BigDecimal;

public class NursingLevel extends BaseEntity {
    private Long id;
    private String name;
    private Long lplanId;
    private String planName;
    private BigDecimal fee;
    private Long status;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public Long getLplanId() {
        return lplanId;
    }

    public void setLplanId(Long v) {
        lplanId = v;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String v) {
        planName = v;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal v) {
        fee = v;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long v) {
        status = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }
}

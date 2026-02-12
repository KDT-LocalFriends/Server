package com.backend.kdt.charge.dto;

import com.backend.kdt.charge.entity.ChargeType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChargeTypeDto {
    private ChargeType type;
    private String displayName;
    private int points;
    private int displayPrice;
}
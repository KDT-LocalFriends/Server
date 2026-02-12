package com.backend.kdt.charge.dto;

import com.backend.kdt.charge.entity.ChargeType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChargeRequestDto {
    private Long userId;
    private ChargeType chargeType;
}

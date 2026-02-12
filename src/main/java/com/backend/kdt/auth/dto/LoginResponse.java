package com.backend.kdt.auth.dto;

import com.backend.kdt.auth.entity.Age;
import com.backend.kdt.auth.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String userName;
    private Gender gender;
    private Age age;
    private Long point;
    private Integer consumptionCount;
    private Integer cosmeticCount;
    private Boolean watched;

    // 꾸미기 아이템 (CONSUMPTION)
    private Integer persimmonCount;
    private Integer greenTeaCount;

    // 성장 아이템 (COSMETIC)
    private Integer strawberryHairpinCount;
    private Integer gongbangAhjimaCount;
    private Integer carCrownCount;
    private Integer roseCount;
}
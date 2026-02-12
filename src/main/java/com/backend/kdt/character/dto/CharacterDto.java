package com.backend.kdt.character.dto;

import com.backend.kdt.character.entity.CharacterType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CharacterDto {
    private Long characterId;
    private Long userId;
    private String characterName;
    private CharacterType characterType;
    private String characterDisplayName;
    private String characterEmoji;
    private Integer level;
    private Long experience;
    private Long maxExperience;
    private Double experiencePercentage;
    private Boolean canLevelUp;

    // 착용 중인 아이템들
    private Boolean equippedStrawberryHairpin;
    private Boolean equippedGongbangAhjima;
    private Boolean equippedCarCrown;
    private Boolean equippedRose;
}
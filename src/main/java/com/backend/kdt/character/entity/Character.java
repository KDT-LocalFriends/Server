package com.backend.kdt.character.entity;

import com.backend.kdt.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "characters")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "character_name", nullable = false, length = 50)
    private String characterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "character_type", nullable = false)
    private CharacterType characterType;

    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "experience", nullable = false)
    @Builder.Default
    private Long experience = 0L;

    @Column(name = "max_experience", nullable = false)
    @Builder.Default
    private Long maxExperience = 100L; // 레벨업에 필요한 경험치

    // 착용 중인 치장품들
    @Column(name = "equipped_strawberry_hairpin")
    @Builder.Default
    private Boolean equippedStrawberryHairpin = false;

    @Column(name = "equipped_gongbang_ahjima")
    @Builder.Default
    private Boolean equippedGongbangAhjima = false;

    @Column(name = "equipped_car_crown")
    @Builder.Default
    private Boolean equippedCarCrown = false;

    @Column(name = "equipped_rose")
    @Builder.Default
    private Boolean equippedRose = false;

    // 경험치 비율 계산
    public double getExperiencePercentage() {
        if (maxExperience == 0) {
            return 0.0;
        }
        return (double) experience / maxExperience * 100.0;
    }

    // 레벨업 체크
    public boolean canLevelUp() {
        return experience >= maxExperience;
    }
}
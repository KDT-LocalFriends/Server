package com.backend.kdt.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "users", indexes = {
        @Index(name = "idx_user_name", columnList = "user_name")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true, length = 50)
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age", nullable = false)
    private Age age;

    // 포인트
    @Column(name = "point")
    @Builder.Default
    private Long point = 0L;

    @Column(name = "consumption_count")
    @Builder.Default
    private Integer consumptionCount = 0;

    @Column(name = "cosmetic_count")
    @Builder.Default
    private Integer cosmeticCount = 0;

    @Column(name = "watched", nullable = false)
    @Builder.Default
    private Boolean watched = false;

    // 꾸미기 아이템 (CONSUMPTION) - 소유 개수만 관리
    @Column(name = "persimmon_count")
    @Builder.Default
    private Integer persimmonCount = 0;

    @Column(name = "green_tea_count")
    @Builder.Default
    private Integer greenTeaCount = 0;

    // 성장 아이템 (COSMETIC) - 소유 개수만 관리 (착용은 Character 엔티티에서)
    @Column(name = "strawberry_hairpin_count")
    @Builder.Default
    private Integer strawberryHairpinCount = 0;

    @Column(name = "gongbang_ahjima_count")
    @Builder.Default
    private Integer gongbangAhjimaCount = 0;

    @Column(name = "car_crown_count")
    @Builder.Default
    private Integer carCrownCount = 0;

    @Column(name = "rose_count")
    @Builder.Default
    private Integer roseCount = 0;

    // 일일 게임 완료 관련 필드
    @Column(name = "daily_game_count")
    @Builder.Default
    private Integer dailyGameCount = 0;  // 일일 게임 완료 횟수

    @Column(name = "last_game_date")
    private LocalDate lastGameDate;  // 마지막 게임 완료 날짜

    // 일일 쓰다듬기 관련 필드
    @Column(name = "daily_pet_count")
    @Builder.Default
    private Integer dailyPetCount = 0;

    @Column(name = "last_pet_date")
    private LocalDate lastPetDate;

    // 먹이주기 관련 필드
    @Column(name = "daily_feed_count")
    @Builder.Default
    private Integer dailyFeedCount = 0;

    @Column(name = "last_feed_date")
    private LocalDate lastFeedDate;

    // 올 컴플릿 보너스 관련 필드
    @Column(name = "last_bonus_date")
    private LocalDate lastBonusDate;
}

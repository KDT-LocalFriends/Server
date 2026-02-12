package com.backend.kdt.pay.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameCompletionResponseDto {
    private Long userId;
    private int experienceGained;        // 획득한 경험치
    private Long totalExperience;        // 총 경험치 (Character의 experience)
    private Integer currentLevel;        // 현재 레벨
    private int dailyGameCount;          // 오늘 완료한 게임 횟수
    private int remainingDailyGames;     // 오늘 남은 게임 횟수
    private String message;              // 완료 메시지
    private LocalDateTime completedAt;   // 완료 시간
}

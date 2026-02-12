package com.backend.kdt.charge.service;

import com.backend.kdt.auth.entity.User;
import com.backend.kdt.auth.repository.UserRepository;
import com.backend.kdt.charge.dto.ChargeResponseDto;
import com.backend.kdt.charge.dto.ChargeTypeDto;
import com.backend.kdt.charge.entity.ChargeType;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChargeService {

    private final UserRepository userRepository;

    @Transactional
    public ChargeResponseDto chargePoints(Long userId, ChargeType chargeType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        // 포인트 충전 (바로 지급)
        Long newPointTotal = user.getPoint() + chargeType.getPoints();
        user.setPoint(newPointTotal);
        userRepository.save(user);

        return ChargeResponseDto.builder()
                .userId(userId)
                .chargeName(chargeType.getDisplayName())
                .chargedPoints(chargeType.getPoints())
                .newTotalPoints(newPointTotal.intValue())
                .chargedAt(LocalDateTime.now())
                .build();
    }

    // 충전 타입 목록 조회
    public List<ChargeTypeDto> getChargeTypes() {
        return Arrays.stream(ChargeType.values())
                .map(type -> ChargeTypeDto.builder()
                        .type(type)
                        .displayName(type.getDisplayName())
                        .points(type.getPoints())
                        .displayPrice(type.getDisplayPrice())
                        .build())
                .collect(Collectors.toList());
    }
}
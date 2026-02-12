package com.backend.kdt.auth.service;

import com.backend.kdt.auth.dto.LoginResponse;
import com.backend.kdt.auth.entity.Age;
import com.backend.kdt.auth.entity.Gender;
import com.backend.kdt.auth.entity.User;
import com.backend.kdt.auth.repository.UserRepository;
import com.backend.kdt.auth.security.JwtService;
import com.backend.kdt.character.service.CharacterService;
import com.backend.kdt.character.entity.CharacterType;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CharacterService characterService;

    public User getUserByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다." + userName));
    }

    public void setLoginCookie(HttpServletResponse response, String userName) {
        jwtService.addAccessTokenCookie(response, userName);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional
    public void resetPassword(String userName, String newPassword) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String encoded = passwordEncoder.encode(newPassword);
        user.setPassword(encoded);
        userRepository.save(user);
    }

    public User getUserByUserNameOrNull(String userName) {
        return userRepository.findByUserName(userName).orElse(null);
    }

    public LoginResponse loginResponse(User user) {
        return LoginResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .gender(user.getGender())
                .age(user.getAge())
                .point(user.getPoint())
                .consumptionCount(user.getConsumptionCount())
                .cosmeticCount(user.getCosmeticCount())
                .watched(user.getWatched())
                .persimmonCount(user.getPersimmonCount())
                .greenTeaCount(user.getGreenTeaCount())
                .strawberryHairpinCount(user.getStrawberryHairpinCount())
                .gongbangAhjimaCount(user.getGongbangAhjimaCount())
                .carCrownCount(user.getCarCrownCount())
                .roseCount(user.getRoseCount())
                .build();
    }

    @Transactional
    public User registerUser(String userName, String password, Gender gender, Age age) {
        if (userRepository.existsByUserName(userName)) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다!");
        }
        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .userName(userName)
                .password(encodedPassword)
                .gender(gender)
                .age(age)
                .point(0L)
                .consumptionCount(0)
                .cosmeticCount(0)
                .watched(false)
                .persimmonCount(0)
                .greenTeaCount(0)
                .strawberryHairpinCount(0)
                .gongbangAhjimaCount(0)
                .carCrownCount(0)
                .roseCount(0)
                // 새로 추가된 필드들 초기화
                .dailyFeedCount(0)
                .lastFeedDate(null)
                .lastBonusDate(null)
                .build();

        User savedUser = userRepository.save(user);

        // 회원가입과 동시에 기본 캐릭터 생성
        try {
            characterService.createCharacter(savedUser.getId(), "알", CharacterType.EGG);
            log.info("사용자 및 기본 캐릭터 생성 완료: {}", savedUser.getUserName());
        } catch (Exception e) {
            log.warn("캐릭터 생성 실패, 나중에 자동 생성됩니다: userId={}, error={}", savedUser.getId(), e.getMessage());
        }

        return savedUser;
    }

    public boolean existsByUserName(String userName) {
        return userRepository.existsByUserName(userName);
    }

    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String userName = ((UserDetails) authentication.getPrincipal()).getUsername();
            return getUserByUserName(userName).getId();
        }
        throw new IllegalArgumentException("User is not authenticated.");
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.: " + userId));
    }

    @Transactional
    public void logoutUser(Long userId, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.save(user);
        jwtService.clearAccessTokenCookie(response);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    @Transactional
    public void setWatched(Long userId, Boolean watched) {
        User user = getUserById(userId);
        user.setWatched(watched);
        userRepository.save(user);
    }
}
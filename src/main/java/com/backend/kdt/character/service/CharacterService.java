package com.backend.kdt.character.service;

import com.backend.kdt.auth.entity.User;
import com.backend.kdt.auth.repository.UserRepository;
import com.backend.kdt.character.dto.CharacterDto;
import com.backend.kdt.character.entity.Character;
import com.backend.kdt.character.entity.CharacterType;
import com.backend.kdt.character.repository.CharacterRepository;
import com.backend.kdt.pay.dto.GameCompletionResponseDto;
import com.backend.kdt.shop.entity.ShopItemType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    // 경험치 상수
    private static final long PET_EXPERIENCE = 20;              // 쓰다듬기 경험치
    private static final long FEED_EXPERIENCE = 20;            // 먹이주기 경험치
    private static final long GAME_COMPLETION_EXPERIENCE = 50;  // 게임 완료 기본 경험치
    private static final long BONUS_EXPERIENCE_3RD_GAME = 20;   // 3번째 게임 완료 시 추가 경험치
    private static final long ALL_COMPLETE_BONUS = 20;         // 올 컴플릿 보너스 경험치

    // 일일 제한 상수
    private static final int DAILY_PET_LIMIT = 3;              // 하루 최대 쓰다듬기 횟수
    private static final int DAILY_FEED_LIMIT = 3;             // 하루 최대 먹이주기 횟수
    private static final int DAILY_GAME_LIMIT = 3;             // 하루 최대 게임 횟수

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    /**
     * 캐릭터 생성 (회원가입 시 호출)
     */
    @Transactional
    public CharacterDto createCharacter(Long userId, String characterName, CharacterType characterType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

        // 이미 캐릭터가 있는지 확인
        if (characterRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("이미 캐릭터가 존재합니다");
        }

        Character character = Character.builder()
                .user(user)
                .characterName(characterName)
                .characterType(characterType) // EGG 또는 DUCK 선택
                .level(1)
                .experience(0L)
                .maxExperience(100L)
                .build();

        Character savedCharacter = characterRepository.save(character);
        return convertToDto(savedCharacter);
    }

    /**
     * 사용자의 캐릭터 조회 (없으면 기본 캐릭터 자동 생성)
     */
    public CharacterDto getCharacterByUserId(Long userId) {
        Character character = characterRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 캐릭터가 없으면 기본 EGG 캐릭터 자동 생성
                    log.info("캐릭터가 없어서 기본 캐릭터 생성: userId={}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

                    Character newCharacter = Character.builder()
                            .user(user)
                            .characterName("알")
                            .characterType(CharacterType.EGG) // 기본은 EGG로 설정
                            .level(1)
                            .experience(0L)
                            .maxExperience(100L)
                            .build();

                    return characterRepository.save(newCharacter);
                });
        return convertToDto(character);
    }

    /**
     * 경험치 추가
     */
    @Transactional
    public CharacterDto addExperience(Long userId, Long exp) {
        Character character = characterRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 캐릭터가 없으면 기본 캐릭터 생성
                    log.info("경험치 추가 중 캐릭터가 없어서 기본 캐릭터 생성: userId={}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

                    Character newCharacter = Character.builder()
                            .user(user)
                            .characterName("알")
                            .characterType(CharacterType.EGG)
                            .level(1)
                            .experience(0L)
                            .maxExperience(100L)
                            .build();

                    return characterRepository.save(newCharacter);
                });

        character.setExperience(character.getExperience() + exp);

        // 자동 레벨업 체크
        while (character.canLevelUp()) {
            levelUp(character);
        }

        Character savedCharacter = characterRepository.save(character);
        log.info("캐릭터 경험치 추가: userId={}, exp={}, newLevel={}", userId, exp, savedCharacter.getLevel());

        return convertToDto(savedCharacter);
    }

    /**
     * 쓰다듬기 처리 및 경험치 지급 (하루 3번 제한)
     */
    @Transactional
    public GameCompletionResponseDto petCharacter(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        // 오늘 날짜로 일일 쓰다듬기 횟수 체크
        LocalDate today = LocalDate.now();

        // 오늘이 마지막 쓰다듬기 날짜와 다르면 카운트 초기화
        if (user.getLastPetDate() == null || !user.getLastPetDate().equals(today)) {
            user.setDailyPetCount(0);
            user.setLastPetDate(today);
        }

        // 일일 쓰다듬기 제한 체크
        if (user.getDailyPetCount() >= DAILY_PET_LIMIT) {
            throw new IllegalStateException("오늘 쓰다듬기 제한 횟수에 도달했습니다. (최대 3회)");
        }

        // 쓰다듬기 횟수 증가
        user.setDailyPetCount(user.getDailyPetCount() + 1);

        // User 엔티티 저장
        userRepository.save(user);

        // 캐릭터에게 경험치 지급
        CharacterDto updatedCharacter = addExperience(userId, PET_EXPERIENCE);

        // 올 컴플릿 보너스 체크
        long bonusExp = checkAndApplyAllCompleteBonus(user);

        String message = String.format("쓰다듬기 완료! 경험치 %d를 획득했습니다. (남은 쓰다듬기: %d회)",
                PET_EXPERIENCE, DAILY_PET_LIMIT - user.getDailyPetCount());

        if (bonusExp > 0) {
            updatedCharacter = addExperience(userId, bonusExp);
            message += String.format(" 🎉 모든 일일 활동을 완료하여 보너스 경험치 %d를 추가로 획득했습니다!", bonusExp);
        }

        return GameCompletionResponseDto.builder()
                .userId(userId)
                .experienceGained((int) (PET_EXPERIENCE + bonusExp))
                .totalExperience(updatedCharacter.getExperience())
                .currentLevel(updatedCharacter.getLevel())
                .dailyGameCount(user.getDailyPetCount())
                .remainingDailyGames(DAILY_PET_LIMIT - user.getDailyPetCount())
                .message(message)
                .completedAt(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 먹이주기 처리 및 소모품 소비 (하루 3번 제한)
     */
    @Transactional
    public GameCompletionResponseDto feedCharacter(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        // 오늘 날짜로 일일 먹이주기 횟수 체크
        LocalDate today = LocalDate.now();

        // 오늘이 마지막 먹이주기 날짜와 다르면 카운트 초기화
        if (user.getLastFeedDate() == null || !user.getLastFeedDate().equals(today)) {
            user.setDailyFeedCount(0);
            user.setLastFeedDate(today);
        }

        // 일일 먹이주기 제한 체크
        if (user.getDailyFeedCount() >= DAILY_FEED_LIMIT) {
            throw new IllegalStateException("오늘 먹이주기 제한 횟수에 도달했습니다. (최대 3회)");
        }

        // 소모품 보유 체크 및 소비 (개수가 많은 것부터)
        String consumedItem = consumeConsumptionItem(user);

        // 먹이주기 횟수 증가
        user.setDailyFeedCount(user.getDailyFeedCount() + 1);

        // User 엔티티 저장
        userRepository.save(user);

        // 캐릭터에게 경험치 지급
        CharacterDto updatedCharacter = addExperience(userId, FEED_EXPERIENCE);

        // 올 컴플릿 보너스 체크
        long bonusExp = checkAndApplyAllCompleteBonus(user);

        String message = String.format("먹이주기 완료! %s을(를) 소비하여 경험치 %d를 획득했습니다. (남은 먹이주기: %d회)",
                consumedItem, FEED_EXPERIENCE, DAILY_FEED_LIMIT - user.getDailyFeedCount());

        if (bonusExp > 0) {
            updatedCharacter = addExperience(userId, bonusExp);
            message += String.format(" 🎉 모든 일일 활동을 완료하여 보너스 경험치 %d를 추가로 획득했습니다!", bonusExp);
        }

        return GameCompletionResponseDto.builder()
                .userId(userId)
                .experienceGained((int) (FEED_EXPERIENCE + bonusExp))
                .totalExperience(updatedCharacter.getExperience())
                .currentLevel(updatedCharacter.getLevel())
                .dailyGameCount(user.getDailyFeedCount())
                .remainingDailyGames(DAILY_FEED_LIMIT - user.getDailyFeedCount())
                .message(message)
                .completedAt(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 소모품 소비 처리 (개수가 많은 것부터)
     */
    private String consumeConsumptionItem(User user) {
        int persimmonCount = user.getPersimmonCount();
        int greenTeaCount = user.getGreenTeaCount();

        // 소모품이 하나도 없는 경우
        if (persimmonCount == 0 && greenTeaCount == 0) {
            throw new IllegalStateException("먹이를 줄 소모품이 없습니다. 영상을 시청하여 소모품을 획득해주세요.");
        }

        // 개수가 많은 것부터 소비
        if (persimmonCount >= greenTeaCount && persimmonCount > 0) {
            user.setPersimmonCount(persimmonCount - 1);
            user.setConsumptionCount(user.getConsumptionCount() - 1);
            return "단감";
        } else if (greenTeaCount > 0) {
            user.setGreenTeaCount(greenTeaCount - 1);
            user.setConsumptionCount(user.getConsumptionCount() - 1);
            return "녹차";
        } else {
            throw new IllegalStateException("먹이를 줄 소모품이 없습니다.");
        }
    }

    /**
     * 올 컴플릿 보너스 체크 및 적용
     */
    private long checkAndApplyAllCompleteBonus(User user) {
        LocalDate today = LocalDate.now();

        // 오늘의 모든 일일 활동이 완료되었는지 체크
        boolean gameComplete = user.getLastGameDate() != null &&
                user.getLastGameDate().equals(today) &&
                user.getDailyGameCount() >= DAILY_GAME_LIMIT;

        boolean petComplete = user.getLastPetDate() != null &&
                user.getLastPetDate().equals(today) &&
                user.getDailyPetCount() >= DAILY_PET_LIMIT;

        boolean feedComplete = user.getLastFeedDate() != null &&
                user.getLastFeedDate().equals(today) &&
                user.getDailyFeedCount() >= DAILY_FEED_LIMIT;

        // 모든 활동이 완료되었고, 아직 보너스를 받지 않은 경우
        if (gameComplete && petComplete && feedComplete) {
            // 오늘 보너스를 이미 받았는지 체크
            if (user.getLastBonusDate() == null || !user.getLastBonusDate().equals(today)) {
                user.setLastBonusDate(today);
                userRepository.save(user);
                log.info("올 컴플릿 보너스 지급: userId={}, bonusExp={}", user.getId(), ALL_COMPLETE_BONUS);
                return ALL_COMPLETE_BONUS;
            }
        }

        return 0;
    }

    /**
     * 게임 완료 처리 및 경험치 지급 (하루 3번 제한)
     */
    @Transactional
    public GameCompletionResponseDto completeGame(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        // 오늘 날짜로 일일 게임 횟수 체크
        LocalDate today = LocalDate.now();

        // 오늘이 마지막 게임 날짜와 다르면 카운트 초기화
        if (user.getLastGameDate() == null || !user.getLastGameDate().equals(today)) {
            user.setDailyGameCount(0);
            user.setLastGameDate(today);
        }

        // 일일 게임 제한 체크
        if (user.getDailyGameCount() >= DAILY_GAME_LIMIT) {
            throw new IllegalStateException("오늘 게임 완료 제한 횟수에 도달했습니다. (최대 3회)");
        }

        // 게임 횟수 증가
        user.setDailyGameCount(user.getDailyGameCount() + 1);

        // 기본 경험치 지급
        long experienceGained = GAME_COMPLETION_EXPERIENCE;
        String message = String.format("게임 완료! 경험치 %d를 획득했습니다.", GAME_COMPLETION_EXPERIENCE);

        // 3번째 게임 완료 시 보너스 경험치
        if (user.getDailyGameCount() == DAILY_GAME_LIMIT) {
            experienceGained += BONUS_EXPERIENCE_3RD_GAME;
            message = String.format("게임 완료! 경험치 %d + 보너스 %d를 획득했습니다! 오늘의 게임 완료 횟수가 모두 소진되었습니다.",
                    GAME_COMPLETION_EXPERIENCE, BONUS_EXPERIENCE_3RD_GAME);
        }

        // User 엔티티 저장
        userRepository.save(user);

        // 캐릭터에게 경험치 지급 (레벨업 자동 처리)
        CharacterDto updatedCharacter = addExperience(userId, experienceGained);

        // 올 컴플릿 보너스 체크 (게임 3회가 완료된 후)
        long bonusExp = checkAndApplyAllCompleteBonus(user);

        if (bonusExp > 0) {
            updatedCharacter = addExperience(userId, bonusExp);
            message += String.format(" 🎉 모든 일일 활동을 완료하여 보너스 경험치 %d를 추가로 획득했습니다!", bonusExp);
        }

        return GameCompletionResponseDto.builder()
                .userId(userId)
                .experienceGained((int) (experienceGained + bonusExp))
                .totalExperience(updatedCharacter.getExperience())
                .currentLevel(updatedCharacter.getLevel())
                .dailyGameCount(user.getDailyGameCount())
                .remainingDailyGames(DAILY_GAME_LIMIT - user.getDailyGameCount())
                .message(message)
                .completedAt(java.time.LocalDateTime.now())
                .build();
    }
    public int getRemainingDailyPets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        LocalDate today = LocalDate.now();

        // 오늘이 아니면 3번 모두 가능
        if (user.getLastPetDate() == null || !user.getLastPetDate().equals(today)) {
            return DAILY_PET_LIMIT;
        }

        return DAILY_PET_LIMIT - user.getDailyPetCount();
    }

    /**
     * 남은 먹이주기 횟수 조회
     */
    public int getRemainingDailyFeeds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 없음"));

        LocalDate today = LocalDate.now();

        // 오늘이 아니면 3번 모두 가능
        if (user.getLastFeedDate() == null || !user.getLastFeedDate().equals(today)) {
            return DAILY_FEED_LIMIT;
        }

        return DAILY_FEED_LIMIT - user.getDailyFeedCount();
    }

    /**
     * 아이템 착용/해제
     */
    @Transactional
    public CharacterDto equipItem(Long userId, ShopItemType itemType, boolean equip) {
        Character character = characterRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 캐릭터가 없으면 기본 캐릭터 생성
                    log.info("아이템 착용/해제 중 캐릭터가 없어서 기본 캐릭터 생성: userId={}", userId);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));

                    Character newCharacter = Character.builder()
                            .user(user)
                            .characterName("알")
                            .characterType(CharacterType.EGG)
                            .level(1)
                            .experience(0L)
                            .maxExperience(100L)
                            .build();

                    return characterRepository.save(newCharacter);
                });

        // 사용자가 해당 아이템을 소유하고 있는지 확인
        User user = character.getUser();
        if (getCosmeticItemCount(user, itemType) <= 0) {
            throw new IllegalStateException("해당 아이템을 소유하고 있지 않습니다");
        }

        // 착용하려는 경우, 같은 종류의 다른 아이템들을 먼저 해제
        if (equip) {
            unequipAllItemsOfSameType(character, itemType);
        }

        // 아이템 착용/해제 처리
        setCosmeticItemEquipped(character, itemType, equip);

        Character savedCharacter = characterRepository.save(character);
        log.info("아이템 {}! userId={}, item={}", equip ? "착용" : "해제", userId, itemType);

        return convertToDto(savedCharacter);
    }

    /**
     * 같은 종류의 모든 아이템 해제 (하나씩만 착용 가능하도록)
     */
    private void unequipAllItemsOfSameType(Character character, ShopItemType newItemType) {
        // 모든 치장품을 해제 (하나씩만 착용 가능)
        character.setEquippedStrawberryHairpin(false);
        character.setEquippedGongbangAhjima(false);
        character.setEquippedCarCrown(false);
        character.setEquippedRose(false);
    }

    /**
     * 레벨업 처리 및 진화 체크
     */
    private void levelUp(Character character) {
        // 진화 체크 (경험치 100 도달 시 EGG -> DUCK)
        if (character.getExperience() >= 100 && character.getCharacterType() == CharacterType.EGG) {
            character.setCharacterType(CharacterType.DUCK);
            character.setCharacterName("토덕이");
            log.info("캐릭터 진화! {} -> {}, 경험치: {}", CharacterType.EGG.getDisplayName(),
                    CharacterType.DUCK.getDisplayName(), character.getExperience());
        }

        character.setLevel(character.getLevel() + 1);
        character.setExperience(character.getExperience() - character.getMaxExperience());

        // 레벨에 따른 최대 경험치 증가 (예: 레벨 * 100)
        character.setMaxExperience((long) (character.getLevel() * 100));

        log.info("레벨업! 캐릭터: {}, 새로운 레벨: {}", character.getCharacterName(), character.getLevel());
    }

    /**
     * 치장품 아이템 착용 상태 설정
     */
    private void setCosmeticItemEquipped(Character character, ShopItemType itemType, boolean equipped) {
        switch (itemType) {
            case STRAWBERRY_HAIRPIN:
                character.setEquippedStrawberryHairpin(equipped);
                break;
            case GONGBANG_AHJIMA:
                character.setEquippedGongbangAhjima(equipped);
                break;
            case CAR_CROWN:
                character.setEquippedCarCrown(equipped);
                break;
            case ROSE:
                character.setEquippedRose(equipped);
                break;
            default:
                throw new IllegalArgumentException("착용할 수 없는 아이템입니다");
        }
    }

    /**
     * 치장품 아이템 착용 여부 조회 (InventoryService에서 호출)
     */
    public boolean getCosmeticItemEquipped(Character character, ShopItemType itemType) {
        switch (itemType) {
            case STRAWBERRY_HAIRPIN:
                return character.getEquippedStrawberryHairpin();
            case GONGBANG_AHJIMA:
                return character.getEquippedGongbangAhjima();
            case CAR_CROWN:
                return character.getEquippedCarCrown();
            case ROSE:
                return character.getEquippedRose();
            default:
                return false;
        }
    }

    /**
     * 사용자의 아이템 소유 개수 조회 (User 엔티티에서)
     */
    private int getCosmeticItemCount(User user, ShopItemType itemType) {
        switch (itemType) {
            case STRAWBERRY_HAIRPIN:
                return user.getStrawberryHairpinCount();
            case GONGBANG_AHJIMA:
                return user.getGongbangAhjimaCount();
            case CAR_CROWN:
                return user.getCarCrownCount();
            case ROSE:
                return user.getRoseCount();
            default:
                return 0;
        }
    }

    /**
     * Character 엔티티를 간단한 DTO로 변환 (경험치와 캐릭터 상태만)
     */
    private CharacterDto convertToDto(Character character) {
        return CharacterDto.builder()
                .characterId(character.getId())
                .userId(character.getUser().getId())
                .characterName(character.getCharacterName())
                .characterType(character.getCharacterType())
                .characterDisplayName(character.getCharacterType().getDisplayName())
                .characterEmoji(character.getCharacterType().getEmoji())
                .level(character.getLevel())
                .experience(character.getExperience())
                .equippedStrawberryHairpin(character.getEquippedStrawberryHairpin())
                .equippedGongbangAhjima(character.getEquippedGongbangAhjima())
                .equippedCarCrown(character.getEquippedCarCrown())
                .equippedRose(character.getEquippedRose())
                .build();
    }
}
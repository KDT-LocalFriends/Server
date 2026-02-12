package com.backend.kdt.character.entity;

public enum CharacterType {
    // 알 캐릭터
    EGG("알", "🥚"),

    // 토덕 캐릭터
    DUCK("토덕이", "🐥");

    private final String displayName;
    private final String emoji;

    CharacterType(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() { return displayName; }
    public String getEmoji() { return emoji; }
}
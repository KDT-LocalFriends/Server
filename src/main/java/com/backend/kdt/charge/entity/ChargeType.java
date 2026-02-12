package com.backend.kdt.charge.entity;

public enum ChargeType {
    COIN_100("코인 100개", 100, 1000),
    COIN_500("코인 500개", 500, 4900),
    COIN_1000("코인 1,000개", 1000, 9500),
    COIN_3000("코인 3,000개", 3000, 27000);

    private final String displayName;
    private final int points;
    private final int displayPrice;

    ChargeType(String displayName, int points, int displayPrice) {
        this.displayName = displayName;
        this.points = points;
        this.displayPrice = displayPrice;
    }

    public String getDisplayName() { return displayName; }
    public int getPoints() { return points; }
    public int getDisplayPrice() { return displayPrice; }
}

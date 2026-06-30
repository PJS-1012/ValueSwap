package com.valueswap.matching.domain;

public enum MatchType {
    ONE_TO_ONE(2),
    THREE_PARTY(3),
    FOUR_PARTY(4);

    private final int participantCount;

    MatchType(int participantCount) {
        this.participantCount = participantCount;
    }

    public static MatchType fromParticipantCount(int count) {
        for (MatchType type : values()) {
            if (type.participantCount == count) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 참여자 수입니다: " + count);
    }
}

package com.xparience.profile.dto;

public record PersonalityQuizRequest(
        String idealWeekendActivity,
        String fictionalDinnerGuest,
        String threeWordsFromFriend,
        String surprisingPassion,
        String emotionalIntelligence
) {}
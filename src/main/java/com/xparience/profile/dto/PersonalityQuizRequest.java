package com.xparience.profile.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PersonalityQuizRequest(
        @NotEmpty(message = "Personality quiz answers are required")
        @Size(min = 15, max = 15, message = "Personality quiz must contain exactly 15 answers")
        List<String> answers,

        String idealWeekendActivity,
        String fictionalDinnerGuest,
        String threeWordsFromFriend,
        String surprisingPassion,
        String emotionalIntelligence
) {}
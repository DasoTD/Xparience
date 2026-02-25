package com.xparience.matching.dto;

import com.xparience.matching.MatchStatus;
import jakarta.validation.constraints.NotNull;
public class MatchActionRequest {
    @NotNull(message = "Action is required")
    private MatchStatus action; // ACCEPTED or REJECTED

    public MatchStatus getAction() {
        return action;
    }

    public void setAction(MatchStatus action) {
        this.action = action;
    }
}
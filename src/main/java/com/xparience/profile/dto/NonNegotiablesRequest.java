package com.xparience.profile.dto;

import jakarta.validation.constraints.NotBlank;

public class NonNegotiablesRequest {
    @NotBlank
    private String nonNegotiable1;
    @NotBlank
    private String nonNegotiable2;
    @NotBlank
    private String nonNegotiable3;

    public String getNonNegotiable1() { return nonNegotiable1; }
    public void setNonNegotiable1(String nonNegotiable1) { this.nonNegotiable1 = nonNegotiable1; }
    public String getNonNegotiable2() { return nonNegotiable2; }
    public void setNonNegotiable2(String nonNegotiable2) { this.nonNegotiable2 = nonNegotiable2; }
    public String getNonNegotiable3() { return nonNegotiable3; }
    public void setNonNegotiable3(String nonNegotiable3) { this.nonNegotiable3 = nonNegotiable3; }
}

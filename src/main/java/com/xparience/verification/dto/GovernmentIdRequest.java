package com.xparience.verification.dto;

import com.xparience.verification.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class GovernmentIdRequest {

    @NotNull(message = "ID type is required")
    private IdType idType;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
}
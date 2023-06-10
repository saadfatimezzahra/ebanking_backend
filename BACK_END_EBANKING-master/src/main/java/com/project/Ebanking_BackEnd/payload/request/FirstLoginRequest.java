package com.project.Ebanking_BackEnd.payload.request;

import javax.validation.constraints.NotBlank;

public class FirstLoginRequest {
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotBlank
    private String email;
}

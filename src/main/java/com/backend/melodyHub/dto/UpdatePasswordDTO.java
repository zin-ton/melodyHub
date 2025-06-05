package com.backend.melodyHub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdatePasswordDTO {
    @NotBlank(message = "Password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]{8,20}$",
            message = "Password must be 8-20 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)"
    )
    private String oldPassword;
    @NotBlank(message = "Password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]{8,20}$",
            message = "Password must be 8-20 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)"
    )
    private String newPassword;

    public UpdatePasswordDTO() {
    }

    public UpdatePasswordDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

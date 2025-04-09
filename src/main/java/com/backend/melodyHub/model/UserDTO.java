package com.backend.melodyHub.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserDTO {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Name cannot be empty")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Only letters are allowed")
    private String firstName;
    @NotBlank(message = "Lastname cannot be empty")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Only letters are allowed")
    private String lastName;
    @NotBlank(message = "Password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]{8,20}$",
            message = "Password must be 8-20 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!)"
    )
    private String password;
    @NotBlank(message = "Username cannot be empty")
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$",
            message = "Username must start with a letter and contain only letters, numbers, and underscores (3-20 characters, no dots allowed)"
    )
    private String login;

    public UserDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String first_name) {
        this.firstName = first_name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String last_name) {
        this.lastName = last_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}

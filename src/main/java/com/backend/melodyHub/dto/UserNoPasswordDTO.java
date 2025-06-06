package com.backend.melodyHub.dto;

import com.backend.melodyHub.model.User;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UserNoPasswordDTO {
    @Nullable
    private Integer id;
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Name cannot be empty")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Only letters are allowed")
    private String firstName;
    @NotBlank(message = "Lastname cannot be empty")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Only letters are allowed")
    private String lastName;
    @NotBlank(message = "Username cannot be empty")
    @Pattern(
            regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$",
            message = "Username must start with a letter and contain only letters, numbers, and underscores (3-20 characters, no dots allowed)"
    )
    private String login;

    public UserNoPasswordDTO() {
    }

    public static UserNoPasswordDTO fromUser(User user) {
        UserNoPasswordDTO userNoPasswordDTO = new UserNoPasswordDTO();
        userNoPasswordDTO.setEmail(user.getEmail());
        userNoPasswordDTO.setFirstName(user.getFirstName());
        userNoPasswordDTO.setLastName(user.getLastName());
        userNoPasswordDTO.setLogin(user.getLogin());
        userNoPasswordDTO.setId(user.getId());
        return userNoPasswordDTO;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}

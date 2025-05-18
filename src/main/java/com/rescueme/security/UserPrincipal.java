package com.rescueme.security;

import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.ShelterStatus;
import com.rescueme.repository.entity.User;
import lombok.Getter;
@Getter
public class UserPrincipal {
    private final Long id;
    private final String username;
    private final String email;
    private final Role role;
    private final ShelterStatus status;
    private final String phoneNumber;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.phoneNumber = user.getPhoneNumber();
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
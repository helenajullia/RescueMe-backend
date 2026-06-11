package com.rescueme.security.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String role;
    private String username;
    private Long id;

    // Fields for shelters
    private String status;
    private Boolean firstLoginAfterApproval;

    // Fields for rejected shelters
    private String rejectionReason;
    private String rejectionDetails;
    private String rejectedAt;

    // Constructor for basic response
    public LoginResponse(String token, String refreshToken, String role, String username, Long id) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.role = role;
        this.username = username;
        this.id = id;
    }
}
package com.awpghost.auth.dto.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String nationality;
    private String mobileNo;
}

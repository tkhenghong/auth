package com.awpghost.auth.dto.responses;

import lombok.Data;

@Data
public class UserInfoDto {
    private String id;
    private String arangoId;
    private String firstName;
    private String lastName;
    private String email;
    private String nationality;
    private String mobileNo;
}

package com.awpghost.auth.dto.responses;

import lombok.Data;

@Data
public class UserInfoDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String location;
}

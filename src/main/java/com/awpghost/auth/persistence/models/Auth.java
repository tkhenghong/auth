package com.awpghost.auth.persistence.models;


import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@NoArgsConstructor
@Data
@Builder
@Document("auth")
public class Auth {
    @Id
    private String id;

    @ArangoId
    private String arangoId;

    private String email;

    private String password;

}

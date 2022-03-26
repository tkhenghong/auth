package com.awpghost.auth.persistence.models;

import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Document("privileges")
public class Privilege extends Auditable {
    @Id
    private String id;

    @ArangoId
    private String arangoId;

    private String name;

}

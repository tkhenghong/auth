package com.awpghost.auth.persistence.models;

import org.springframework.data.annotation.*;

import java.time.LocalDateTime;

public class Auditable {
    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @Version
    private Long version;

    /**
     * Empty constructor is because create an Auditable is not necessary.
     */
    public Auditable() {
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return this.createdDate;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public Long getVersion() {
        return this.version;
    }
}

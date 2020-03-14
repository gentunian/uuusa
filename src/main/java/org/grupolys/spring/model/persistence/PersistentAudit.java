package org.grupolys.spring.model.persistence;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Data
public class PersistentAudit {
    @CreatedBy
    private String user;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedBy
    private String lastModifiedUser;
    @LastModifiedDate
    private Instant lastModifiedDate;
}

package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores public contact card links and recipient mailbox.
 */
@Getter
@Setter
@Entity
@Table(name = "contact_profiles")
public class ContactProfile extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 320)
    private String linkedinUrl;

    @Column(length = 320)
    private String githubUrl;

    @Column(length = 320)
    private String mediumUrl;

    @Column(nullable = false, length = 180)
    private String recipientEmail;
}

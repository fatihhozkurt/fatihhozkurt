package com.fatihozkurt.fatihozkurtcom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores inbound contact form submissions.
 */
@Getter
@Setter
@Entity
@Table(name = "contact_messages")
public class ContactMessage extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ContactMessageStatus status = ContactMessageStatus.RECEIVED;
}

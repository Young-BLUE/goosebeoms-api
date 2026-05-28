package com.goosebeoms.tickets.domain.user.entity;

import com.goosebeoms.tickets.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private int tokenVersion;

    @Getter
    public enum Role {
        USER("일반"),
        ADMIN("관리자");

        private final String label;
        Role(String label) { this.label = label; }
    }

    @Builder
    private User(String email, String password, String name, String phone, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role != null ? role : Role.USER;
        this.tokenVersion = 1;
    }

    public void changePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
        this.tokenVersion++;
    }
}

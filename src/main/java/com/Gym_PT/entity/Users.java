package com.Gym_PT.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = { @Index(name = "idx_users_google_sub", columnList = "google_sub") },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_google_sub", columnNames = "google_sub"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Users {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 구글 로그인 식별용 (절대 값 바꾸지 않는 쪽으로)
    @Column(name = "google_sub", nullable = false, length = 64)
    private String googleSub;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    // 🔹 구글에서 받은 원본 이름
    @Column(name = "name", length = 100)
    private String name;

    // 🔹 구글에서 받은 원본 프로필 사진
    @Column(name = "picture", length = 512)
    private String picture;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    // 우리 서비스에서만 쓰는 표시 이름 (사용자가 수정할 수 있는 필드)
    @Column(name = "display_name", length = 100)
    private String displayName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 필요하면 편의 메서드
    @PrePersist
    public void initDisplayName() {
        if (this.displayName == null || this.displayName.isBlank()) {
            this.displayName = this.name; // 처음에는 구글 이름으로 초기화
        }
    }
}


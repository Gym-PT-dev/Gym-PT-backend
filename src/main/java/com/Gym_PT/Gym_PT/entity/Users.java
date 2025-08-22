package com.Gym_PT.Gym_PT.entity;

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

    @Column(name = "google_sub", nullable = false, length = 64)
    private String googleSub;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "picture", length = 512)
    private String picture;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // (선택) 양방향이 필요할 때만 열기
    // @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    // private UserProfile profile;
}

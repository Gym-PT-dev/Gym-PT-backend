package com.Gym_PT.Gym_PT.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 계정과 1:1 (필수), user_id에 unique index 권장 */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Float height;
    private Float weight;

    @Enumerated(EnumType.STRING)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    /** 예: 주당 운동 시간(분) */
    private Integer duration;

    private String location;
    private String splitStrategy;

    public enum Gender { MALE, FEMALE }
    public enum Goal { DIET, BULK, KEEP }
    public enum ExperienceLevel { BEGINNER, INTERMEDIATE, ADVANCED }
}

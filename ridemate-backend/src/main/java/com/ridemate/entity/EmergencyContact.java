package com.ridemate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emergency_contacts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 60)
    private String relation;
}

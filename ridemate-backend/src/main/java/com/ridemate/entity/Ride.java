package com.ridemate.entity;

import com.ridemate.enums.ParcelSize;
import com.ridemate.enums.RideStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rides")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "from_location", nullable = false, length = 255)
    private String fromLocation;

    @Column(name = "to_location", nullable = false, length = 255)
    private String toLocation;

    @Column(name = "from_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal fromLat;

    @Column(name = "from_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal fromLng;

    @Column(name = "to_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal toLat;

    @Column(name = "to_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal toLng;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "seats_total", nullable = false)
    private int seatsTotal;

    @Column(name = "seats_available", nullable = false)
    private int seatsAvailable;

    @Column(name = "price_per_seat", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerSeat;

    @Column(name = "accepts_passengers", nullable = false)
    @Builder.Default
    private boolean acceptsPassengers = true;

    @Column(name = "accepts_parcels", nullable = false)
    @Builder.Default
    private boolean acceptsParcels = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "max_parcel_size")
    private ParcelSize maxParcelSize;

    @Column(name = "women_only", nullable = false)
    @Builder.Default
    private boolean womenOnly = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RideStatus status = RideStatus.SCHEDULED;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RideStop> stops = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

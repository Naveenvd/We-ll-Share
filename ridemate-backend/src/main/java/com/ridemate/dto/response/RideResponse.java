package com.ridemate.dto.response;

import com.ridemate.enums.ParcelSize;
import com.ridemate.enums.RideStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class RideResponse {

    private Long id;
    private DriverSummaryResponse driver;
    private VehicleResponse vehicle;

    private String fromLocation;
    private String toLocation;
    private BigDecimal fromLat;
    private BigDecimal fromLng;
    private BigDecimal toLat;
    private BigDecimal toLng;

    private LocalDateTime departureTime;
    private int seatsTotal;
    private int seatsAvailable;
    private BigDecimal pricePerSeat;

    private boolean acceptsPassengers;
    private boolean acceptsParcels;
    private ParcelSize maxParcelSize;
    private boolean womenOnly;

    private RideStatus status;
    private List<RideStopResponse> stops;
    private LocalDateTime createdAt;
}

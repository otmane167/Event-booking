package com.event.booking.model.entity.event;

import com.event.booking.model.entity.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false)
    @NotBlank(message = "Title is required")
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Venue is required")
    private String venue;

    @Column(nullable = false)
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
}
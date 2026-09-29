package com.event.booking.model.entity.TicketType;

import com.event.booking.model.entity.event.Event;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "ticket_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, nullable = false)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(nullable = false)
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;

    @Column(nullable = false)
    @Min(value = 0, message = "Quantity available cannot be negative")
    private Integer quantityAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Version
    private Long version;
}
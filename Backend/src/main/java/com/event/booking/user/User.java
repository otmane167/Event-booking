package com.event.booking.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", length = 64, nullable = false)
    @NotBlank(message = "Nom is required")
    private String nom;

    @Column(name = "prenom", length = 64, nullable = false)
    @NotBlank(message = "Prenom is required")
    private String prenom;

    @Column(length = 128, nullable = false, unique = true)
    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(length = 64, nullable = false)
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "Password must contain at least one letter, one digit, one special character, and be at least 8 characters long."
    )
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
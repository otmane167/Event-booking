package com.event.booking.model.entity.User;

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
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
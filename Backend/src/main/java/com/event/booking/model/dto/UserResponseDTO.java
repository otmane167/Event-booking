package com.event.booking.model.dto;

import com.event.booking.model.entity.User.Role;

public record UserResponseDTO(
        Long id,
        String nom,
        String prenom,
        String email,
        Role role
) {}
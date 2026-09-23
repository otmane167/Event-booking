package com.event.booking.controller;

import com.event.booking.model.dto.UserResponseDTO;
import com.event.booking.model.entity.User.User;
import com.event.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponseDTO create(@Valid @RequestBody User user) {
        User saved = userService.create(user);
        return userService.toDto(saved);
    }

    @GetMapping
    public List<UserResponseDTO> findAll() {
        return userService.findAll()
                .stream()
                .map(userService::toDto)
                .collect(Collectors.toList());
    }
}
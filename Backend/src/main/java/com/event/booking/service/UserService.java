package com.event.booking.service;

import com.event.booking.model.dto.UserResponseDTO;
import org.springframework.stereotype.Service;

import com.event.booking.model.entity.User.User;
import java.util.List;

public interface UserService {
    User create(User user);
    List<User> findAll();
    User findById(Long id);
    UserResponseDTO toDto(User user);
}

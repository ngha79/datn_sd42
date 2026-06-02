package com.base.service;

import com.base.dto.request.user.ChangePasswordRequest;
import com.base.dto.request.user.UpdateUserRequest;
import com.base.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public interface UserService {
    Map<String, Object> getCurrentUser(UserDetails userDetails);

    Page<User> getAllUsers(Pageable pageable);

    User getUserById(Long id);

    void updateProfile(UpdateUserRequest request);

    void changePassword(ChangePasswordRequest request, UserDetails userDetails);
}

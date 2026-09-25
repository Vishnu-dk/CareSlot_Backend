package com.careslot.controller;

import com.careslot.db.generated.tables.records.UsersRecord;
import com.careslot.dto.auth.ApiResponse;
import com.careslot.dto.auth.UserResponse;
import com.careslot.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminService.getAllActiveUsers().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable UUID userId) {
        adminService.deactivateUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("User Deactivated"));    }

    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<ApiResponse> activateUser(@PathVariable UUID userId) {
        adminService.activateUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("User Activated"));    }

    private UserResponse mapToResponse(UsersRecord record) {

        return UserResponse.builder()
                .id(record.getId())
                .email(record.getEmail())
                .role(record.getRole())
                .createdAt(record.getCreatedAt().toLocalDate())
                .build();
    }
}
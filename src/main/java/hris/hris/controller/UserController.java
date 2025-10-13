package hris.hris.controller;

import hris.hris.dto.ApiResponse;
import hris.hris.dto.PasswordUpdateRequest;
import hris.hris.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("Password update request for user: {}", email);

        userService.updatePassword(email, request);

        log.info("Password updated successfully for user: {}", email);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully"));
    }
}
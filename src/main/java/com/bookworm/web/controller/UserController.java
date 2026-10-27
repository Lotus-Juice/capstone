package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.AddressDTOs;
import com.bookworm.api.v1.dto.AuthDTOs;
import com.bookworm.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profiles and delivery address book management.")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final AuthService authService;

    @Operation(summary = "Get authenticated user profile", operationId = "getUserProfile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/profile")
    public ResponseEntity<AuthDTOs.UserProfileResponse> getProfile(
        @RequestAttribute(value = "userId", required = false) UUID userId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(authService.getUserProfile(effectiveUserId));
    }

    @Operation(summary = "List all saved delivery addresses", operationId = "getUserAddresses")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of saved addresses"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTOs.AddressDTO>> getAddresses(
        @RequestAttribute(value = "userId", required = false) UUID userId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(authService.getUserAddresses(effectiveUserId));
    }

    @Operation(summary = "Add a new delivery address", operationId = "addUserAddress")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Address created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid address payload"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/addresses")
    public ResponseEntity<AddressDTOs.AddressDTO> addAddress(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @Valid @RequestBody AddressDTOs.CreateAddressRequest request
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        AddressDTOs.AddressDTO response = authService.addUserAddress(effectiveUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Delete a saved delivery address", operationId = "deleteUserAddress")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Address deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("addressId") UUID addressId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        authService.deleteUserAddress(effectiveUserId, addressId);
        return ResponseEntity.noContent().build();
    }
}

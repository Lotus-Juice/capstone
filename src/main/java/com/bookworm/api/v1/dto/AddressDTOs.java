package com.bookworm.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

public final class AddressDTOs {

    private AddressDTOs() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateAddressRequest {

        @NotBlank(message = "Recipient name is required")
        @Size(max = 200, message = "Recipient name cannot exceed 200 characters")
        private String recipientName;

        @NotBlank(message = "Recipient phone is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        private String recipientPhone;

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 cannot exceed 255 characters")
        private String addressLine1;

        @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
        private String addressLine2;

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        private String city;

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State cannot exceed 100 characters")
        private String state;

        @NotBlank(message = "Pincode is required")
        @Pattern(regexp = "^\\d{6}$", message = "Pincode must be exactly 6 digits")
        private String pincode;

        @Builder.Default
        private String country = "India";

        @Builder.Default
        private boolean isDefault = false;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDTO {
        private UUID id;
        private String recipientName;
        private String recipientPhone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String country;
        private boolean isDefault;
    }
}

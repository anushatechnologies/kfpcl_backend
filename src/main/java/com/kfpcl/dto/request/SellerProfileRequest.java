package com.kfpcl.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 150, message = "Company name must be between 2 and 150 characters")
    private String companyName;

    @Size(max = 50, message = "Business registration number cannot exceed 50 characters")
    private String businessRegistrationNumber;

    @Size(max = 50, message = "Tax ID / GST cannot exceed 50 characters")
    private String taxId;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private String logoUrl;

    private String bannerUrl;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @Min(value = 1800, message = "Year established must be valid")
    @Max(value = 2100, message = "Year established must be valid")
    private Integer yearEstablished;
}

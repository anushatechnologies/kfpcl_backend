package com.kfpcl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusUpdateDto {

    @NotBlank(message = "Status is required (OPEN, IN_PROGRESS, RESOLVED)")
    private String status;
}

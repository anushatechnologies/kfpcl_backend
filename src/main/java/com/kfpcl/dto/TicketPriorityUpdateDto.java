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
public class TicketPriorityUpdateDto {

    @NotBlank(message = "Priority is required (LOW, MEDIUM, HIGH, URGENT)")
    private String priority;
}

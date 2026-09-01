package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDto {
    private boolean newInquiry;
    private boolean orderUpdates;
    private boolean rfqUpdates;
    private boolean stockAlerts;
    private boolean paymentUpdates;
    private boolean whatsappEnabled;
}

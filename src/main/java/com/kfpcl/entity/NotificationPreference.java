package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences", indexes = {
        @Index(name = "idx_notif_pref_user", columnList = "user_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true, length = 64)
    private String userId;

    @Column(name = "new_inquiry")
    @Builder.Default
    private boolean newInquiry = true;

    @Column(name = "order_updates")
    @Builder.Default
    private boolean orderUpdates = true;

    @Column(name = "rfq_updates")
    @Builder.Default
    private boolean rfqUpdates = true;

    @Column(name = "stock_alerts")
    @Builder.Default
    private boolean stockAlerts = true;

    @Column(name = "payment_updates")
    @Builder.Default
    private boolean paymentUpdates = true;

    @Column(name = "whatsapp_enabled")
    @Builder.Default
    private boolean whatsappEnabled = false;
}

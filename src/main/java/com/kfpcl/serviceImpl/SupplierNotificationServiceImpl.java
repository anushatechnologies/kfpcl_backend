package com.kfpcl.serviceImpl;

import com.kfpcl.entity.Notification;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.Supplier;
import com.kfpcl.repository.NotificationRepository;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.SupplierRepository;
import com.kfpcl.service.SupplierNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierNotificationServiceImpl implements SupplierNotificationService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notifyEligibleSuppliers(Rfq rfq) {
        Set<Supplier> eligibleSuppliers = new HashSet<>();

        // 1. Supplier of the specific product
        if (rfq.getProduct() != null && rfq.getProduct().getSupplier() != null) {
            eligibleSuppliers.add(rfq.getProduct().getSupplier());
        }

        // 2. Suppliers offering products in the same category
        if (rfq.getCategory() != null) {
            List<Product> categoryProducts = productRepository.findByCategoryIdAndStatus(rfq.getCategory().getId(), Product.Status.ACTIVE);
            for (Product p : categoryProducts) {
                if (p.getSupplier() != null) {
                    eligibleSuppliers.add(p.getSupplier());
                }
            }
        }

        // 3. If still empty, notify all verified suppliers
        if (eligibleSuppliers.isEmpty()) {
            eligibleSuppliers.addAll(supplierRepository.findAll());
        }

        for (Supplier supplier : eligibleSuppliers) {
            if (supplier.getUser() != null) {
                Notification notification = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .recipientUserId(supplier.getUser().getId())
                        .title("New RFQ Opportunity: " + rfq.getProductTitle())
                        .message(String.format("Buyer requested a quote for %d %s of '%s' by %s. Target price: %s",
                                rfq.getQuantity(), rfq.getUnit(), rfq.getProductTitle(),
                                rfq.getExpectedDeliveryDate(),
                                rfq.getTargetPrice() != null ? "INR " + rfq.getTargetPrice() : "Open"))
                        .type("NEW_RFQ")
                        .isRead(false)
                        .build();

                notificationRepository.save(notification);
                log.info("Dispatched notification for RFQ {} to supplier user {}", rfq.getId(), supplier.getUser().getId());
            }
        }
    }
}

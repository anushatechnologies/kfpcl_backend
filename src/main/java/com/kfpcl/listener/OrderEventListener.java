package com.kfpcl.listener;

import com.kfpcl.event.OrderCreationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    /**
     * Listens for OrderCreationEvent emitted upon successful quote acceptance.
     * Integrates with Order Service / Kafka / RabbitMQ.
     */
    @EventListener
    public void handleOrderCreationEvent(OrderCreationEvent event) {
        log.info("================ [ORDER CREATION EVENT EMITTED] ================");
        log.info("Event ID: {}", event.getEventId());
        log.info("RFQ: #{} - '{}'", event.getRfqId(), event.getRfqTitle());
        log.info("Winning Quote ID: {}", event.getQuotationId());
        log.info("Buyer: #{} [{}] ({})", event.getBuyerId(), event.getBuyerCompanyName(), event.getBuyerEmail());
        log.info("Seller: #{} [{}] ({})", event.getSellerId(), event.getSellerCompanyName(), event.getSellerEmail());
        log.info("Quantity: {} {}", event.getQuantity(), event.getUnit());
        log.info("Unit Price: INR {}", event.getUnitPrice());
        log.info("Freight: INR {}, Tax: INR {}", event.getFreightCharges(), event.getTaxAmount());
        log.info("Total Order Landed Amount: INR {}", event.getTotalAmount());
        log.info("Delivery Location: {}", event.getDeliveryLocation());
        log.info("Payment Terms: {}", event.getPaymentTerms());
        log.info("Delivery Lead Time: {} days", event.getDeliveryTimelineDays());
        log.info("=================================================================");
    }
}

package com.payment.gateway;

import com.payment.entity.enums.PaymentGatewayType;
import com.payment.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayFactory {

    private final Map<PaymentGatewayType, PaymentGateway> gatewayMap;

    public PaymentGatewayFactory(List<PaymentGateway> gateways) {
        this.gatewayMap = gateways.stream()
                .collect(Collectors.toMap(PaymentGateway::getGatewayType, Function.identity()));
    }

    public PaymentGateway getGateway(PaymentGatewayType type) {
        if (type == null || type == PaymentGatewayType.NONE) {
            throw new BadRequestException("Invalid or unsupported payment gateway: " + type);
        }
        PaymentGateway gateway = gatewayMap.get(type);
        if (gateway == null) {
            throw new BadRequestException("No payment gateway registered for type: " + type);
        }
        return gateway;
    }
    
}

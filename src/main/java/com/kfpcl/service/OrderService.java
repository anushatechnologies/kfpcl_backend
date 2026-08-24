package com.kfpcl.service;

import com.kfpcl.dto.BuyerOrderResponse;
import com.kfpcl.dto.OrderTrackingResponse;
import com.kfpcl.entity.Order;
import com.kfpcl.entity.Quotation;

import java.util.List;

public interface OrderService {

    Order createOrderFromQuotation(Quotation quotation, String shippingAddress);

    List<BuyerOrderResponse> getBuyerOrders(String status);

    BuyerOrderResponse getBuyerOrderById(String orderId);

    BuyerOrderResponse cancelOrder(String orderId);

    BuyerOrderResponse confirmDelivery(String orderId);

    OrderTrackingResponse getOrderTracking(String orderId);
}

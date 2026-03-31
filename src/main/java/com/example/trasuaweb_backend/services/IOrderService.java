package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.CheckoutRequest;
import com.example.trasuaweb_backend.dtos.responses.OrderResponse;

import java.util.List;

public interface IOrderService {
    OrderResponse placeOrder(CheckoutRequest request, String userEmail);
    OrderResponse getOrderDetails(String orderNo, String sessionId, String userEmail);
    List<OrderResponse> getMyOrders(String userEmail);
    OrderResponse trackOrder(String orderNo, String phone);
}

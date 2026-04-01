package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.CheckoutRequest;
import com.example.trasuaweb_backend.dtos.responses.OrderResponse;
import com.example.trasuaweb_backend.dtos.responses.PageResponse;

import java.util.List;

public interface IOrderService {
    OrderResponse placeOrder(CheckoutRequest request, String userEmail);
    OrderResponse getOrderDetails(String orderNo, String sessionId, String userEmail);
    List<OrderResponse> getMyOrders(String userEmail);
    OrderResponse trackOrder(String orderNo, String phone);
    PageResponse<OrderResponse> getAllOrdersForAdmin(int page, int size, String status);
    OrderResponse updateOrderStatus(Long orderId, String newStatus, String cancelReason);
}

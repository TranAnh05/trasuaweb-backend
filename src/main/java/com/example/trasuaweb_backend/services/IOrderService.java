package com.example.trasuaweb_backend.services;

import com.example.trasuaweb_backend.dtos.requests.CheckoutRequest;
import com.example.trasuaweb_backend.dtos.responses.OrderResponse;

public interface IOrderService {
    OrderResponse placeOrder(CheckoutRequest request, String userEmail);
}

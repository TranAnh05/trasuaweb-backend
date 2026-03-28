package com.example.trasuaweb_backend.controllers;

import com.example.trasuaweb_backend.dtos.responses.ApiResponse;
import com.example.trasuaweb_backend.dtos.responses.ToppingResponse;
import com.example.trasuaweb_backend.entities.Topping;
import com.example.trasuaweb_backend.repositories.ToppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/toppings")
@RequiredArgsConstructor
public class ToppingController {

    private final ToppingRepository toppingRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ToppingResponse>>> getActiveToppings() {
        List<Topping> toppings = toppingRepository.findAllByInStockTrueAndStatus("active");

        List<ToppingResponse> responseList = toppings.stream()
                .map(t -> ToppingResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .price(t.getPrice())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<ToppingResponse>>builder()
                .status(200)
                .message("Thành công")
                .data(responseList)
                .build());
    }
}
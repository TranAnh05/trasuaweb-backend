package com.example.trasuaweb_backend.dtos.responses;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String defaultImage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isNew; // Dùng để gắn badge "NEW" trên UI
}

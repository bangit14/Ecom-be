package com.vn.auth.web.store.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {
    private UUID id;
    private String name;
    private String taxNumber;
    private String city;
    private String description;
    private String logo;
    private String banner;
    private String status;
    private LocalDateTime createdAt;
}

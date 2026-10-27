package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.CartDTOs;
import com.bookworm.api.v1.dto.OrderDTOs;
import com.bookworm.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Checkout pipeline, order state management, order history, and 1-click reorder.")
@SecurityRequirement(name = "BearerAuth")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "List authenticated customer order history ('My Orders')", operationId = "listOrders")
    @GetMapping
    public ResponseEntity<Page<OrderDTOs.OrderHistorySummaryDTO>> listOrders(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResponseEntity.ok(orderService.listOrders(effectiveUserId, pageable));
    }

    @Operation(summary = "Create order from active shopping cart (Checkout)", operationId = "createOrder")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order placed successfully"),
        @ApiResponse(responseCode = "400", description = "Empty cart or invalid coupon"),
        @ApiResponse(responseCode = "409", description = "Insufficient inventory")
    })
    @PostMapping
    public ResponseEntity<OrderDTOs.OrderDetailResponse> createOrder(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody OrderDTOs.CreateOrderRequest request
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        OrderDTOs.OrderDetailResponse response = orderService.checkoutOrder(effectiveUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get detailed order information", operationId = "getOrderDetails")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTOs.OrderDetailResponse> getOrder(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("orderId") UUID orderId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(orderService.getOrderDetails(effectiveUserId, orderId));
    }

    @Operation(summary = "Re-purchase items from past order ('Buy It Again')", operationId = "reorderOrder")
    @PostMapping("/{orderId}/reorder")
    public ResponseEntity<CartDTOs.CartDTO> reorder(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("orderId") UUID orderId
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(orderService.reorder(effectiveUserId, orderId));
    }

    @Operation(summary = "Cancel order before shipment", operationId = "cancelOrder")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTOs.CancelOrderResponse> cancelOrder(
        @RequestAttribute(value = "userId", required = false) UUID userId,
        @PathVariable("orderId") UUID orderId,
        @Valid @RequestBody OrderDTOs.CancelOrderRequest request
    ) {
        UUID effectiveUserId = (userId != null) ? userId : UUID.fromString("d290f1ee-6c54-4b01-90e6-d701748f0851");
        return ResponseEntity.ok(orderService.cancelOrder(effectiveUserId, orderId, request));
    }
}

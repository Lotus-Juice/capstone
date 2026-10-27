package com.bookworm.web.controller;

import com.bookworm.api.v1.dto.CartDTOs.CartDTO;
import com.bookworm.api.v1.dto.OrderDTOs.*;
import com.bookworm.core.service.OrderService;
import com.bookworm.web.advice.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Web Unit Tests")
class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderDetailResponse sampleOrderDetail;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        sampleOrderDetail = OrderDetailResponse.builder()
            .orderId(UUID.randomUUID())
            .orderNumber("BW-2025-55555")
            .status("CONFIRMED")
            .grandTotal(new BigDecimal("999.00"))
            .currency("INR")
            .build();
    }

    @Nested
    @DisplayName("GET /api/v1/orders")
    class ListOrdersTests {

        @Test
        @DisplayName("Should return paginated order history with 200 OK")
        void shouldListOrders() throws Exception {
            OrderHistorySummaryDTO summary = OrderHistorySummaryDTO.builder()
                .orderId(UUID.randomUUID())
                .orderNumber("BW-2025-55555")
                .status("CONFIRMED")
                .grandTotal(new BigDecimal("999.00"))
                .build();

            Page<OrderHistorySummaryDTO> page = new PageImpl<>(List.of(summary));
            given(orderService.listOrders(any(), any(Pageable.class))).willReturn(page);

            mockMvc.perform(get("/api/v1/orders")
                    .param("page", "1")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("BW-2025-55555"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/orders")
    class CreateOrderTests {

        @Test
        @DisplayName("Should checkout order with 201 Created")
        void shouldCreateOrder() throws Exception {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setCartId(UUID.randomUUID());
            request.setShippingAddressId(UUID.randomUUID());

            given(orderService.checkoutOrder(any(), any(CreateOrderRequest.class))).willReturn(sampleOrderDetail);

            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("BW-2025-55555"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/orders/{orderId}/cancel")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order with 200 OK")
        void shouldCancelOrder() throws Exception {
            UUID orderId = UUID.randomUUID();
            CancelOrderRequest request = new CancelOrderRequest();
            request.setReason("Customer request");

            CancelOrderResponse cancelResponse = CancelOrderResponse.builder()
                .orderId(orderId)
                .status("CANCELLED")
                .refundInitiated(true)
                .refundAmount(new BigDecimal("999.00"))
                .build();

            given(orderService.cancelOrder(any(), eq(orderId), any(CancelOrderRequest.class))).willReturn(cancelResponse);

            mockMvc.perform(post("/api/v1/orders/" + orderId + "/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundInitiated").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/orders/{orderId}/reorder")
    class ReorderTests {

        @Test
        @DisplayName("Should reorder items from past order with 200 OK")
        void shouldReorder() throws Exception {
            UUID orderId = UUID.randomUUID();
            CartDTO cartDTO = CartDTO.builder()
                .cartId(UUID.randomUUID())
                .status("ACTIVE")
                .itemCount(2)
                .build();

            given(orderService.reorder(any(), eq(orderId))).willReturn(cartDTO);

            mockMvc.perform(post("/api/v1/orders/" + orderId + "/reorder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.itemCount").value(2));
        }
    }
}

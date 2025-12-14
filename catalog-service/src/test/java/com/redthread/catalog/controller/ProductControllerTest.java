package com.redthread.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redthread.catalog.controller.dto.CreateProductReq;
import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.enums.ProductGender;
import com.redthread.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean ProductService service;

    @Test
    void create_returns201() throws Exception {
        Product p = Product.builder()
                .id(1L)
                .name("Polera")
                .basePrice(new BigDecimal("10000"))
                .featured(true)
                .gender(ProductGender.HOMBRE)
                .build();

        when(service.create(anyLong(), any(), anyString(), any(),
                any(), anyBoolean(), any()))
                .thenReturn(p);

        CreateProductReq req = new CreateProductReq(
                1L, null, "Polera", null,
                new BigDecimal("10000"),
                true,
                ProductGender.HOMBRE
        );

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.featured").value(true));
    }

    @Test
    void list_featured_returns200() throws Exception {
        when(service.list(null, null, true))
                .thenReturn(List.of(
                        Product.builder().id(1L).featured(true).build()
                ));

        mvc.perform(get("/products").param("featured", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getById_returns200() throws Exception {
        when(service.get(5L))
                .thenReturn(Product.builder().id(5L).build());

        mvc.perform(get("/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));
    }
}

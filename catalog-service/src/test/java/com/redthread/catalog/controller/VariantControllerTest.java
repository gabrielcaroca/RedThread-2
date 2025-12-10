package com.redthread.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redthread.catalog.controller.dto.CreateVariantReq;
import com.redthread.catalog.model.Product;
import com.redthread.catalog.model.Variant;
import com.redthread.catalog.model.enums.SizeType;
import com.redthread.catalog.repository.VariantRepository;
import com.redthread.catalog.service.VariantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VariantController.class)
@AutoConfigureMockMvc(addFilters = false)
class VariantControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean VariantService service;
    @MockBean VariantRepository repo;

    @Test
    void create_returns201() throws Exception {
        Variant v = Variant.builder()
                .id(5L)
                .product(Product.builder().id(10L).build())
                .sizeType(SizeType.LETTER)
                .sku("SKU-1")
                .color("NEGRO")
                .sizeValue("M")
                .build();

        when(service.create(any(CreateVariantReq.class))).thenReturn(v);

        CreateVariantReq req = new CreateVariantReq(
                10L,
                SizeType.LETTER,
                "M",
                "NEGRO",
                null,
                2
        );

        mvc.perform(post("/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void list_byProduct_returnsList() throws Exception {
        when(service.byProduct(10L)).thenReturn(List.of(
                Variant.builder().id(1L).build(),
                Variant.builder().id(2L).build()
        ));

        mvc.perform(get("/variants").param("productId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].id").value(2L));
    }
}

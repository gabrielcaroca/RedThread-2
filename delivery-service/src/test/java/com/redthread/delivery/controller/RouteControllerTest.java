package com.redthread.delivery.controller;

import com.redthread.delivery.domain.DeliveryRoute;
import com.redthread.delivery.security.AuthUtils;
import com.redthread.delivery.service.RouteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerTest {

    @Autowired MockMvc mvc;

    @MockBean RouteService routeService;
    @MockBean AuthUtils auth;

    @Test
    void active_returns200() throws Exception {
        when(routeService.activeRoutes()).thenReturn(List.of(
                DeliveryRoute.builder().id(1L).nombre("R1").descripcion("").totalPedidos(1).totalPrice(1000L).activa(true).build()
        ));

        Jwt principal = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("user_id", 1L)
                .claim("roles", List.of("ADMIN"))
                .build();

        mvc.perform(get("/routes/active").with(jwt().jwt(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}

package com.innova.catalogservice.web.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.innova.catalogservice.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Sql("/test-data.sql")
class ProductControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldReturnProducts() throws Exception {
        mockMvc.perform(get("/api/products").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(false)))
                .andExpect(jsonPath("$.hasNext", is(true)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldGetProductByCode() throws Exception {
        mockMvc.perform(get("/api/products/{code}", "P100").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("P100")))
                .andExpect(jsonPath("$.name", is("The Hunger Games")))
                .andExpect(jsonPath("$.description", is("Winning will make you famous. Losing means certain death...")))
                .andExpect(jsonPath("$.price", is(34.0)));
    }

    @Test
    void shouldReturnNotFoundWhenProductCodeNotExists() throws Exception {
        String code = "invalid_product_code";
        mockMvc.perform(get("/api/products/{code}", code).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("Product Not Found")))
                .andExpect(jsonPath("$.detail", is("Product not found " + code)));
    }
}

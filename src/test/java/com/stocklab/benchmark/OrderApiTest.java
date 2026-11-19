package com.stocklab.benchmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "benchmark.strategy=e1")
@AutoConfigureMockMvc
class OrderApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUnknownOrder_returns404() throws Exception {
        mockMvc.perform(get("/order/not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUnknownStock_returns404() throws Exception {
        mockMvc.perform(get("/stock/getStock/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithoutRequestId_returns400() throws Exception {
        mockMvc.perform(post("/order/create")
                        .param("skuId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithInvalidSkuId_returns400() throws Exception {
        mockMvc.perform(post("/order/create")
                        .param("requestId", "test-api-1")
                        .param("skuId", "0"))
                .andExpect(status().isBadRequest());
    }
}

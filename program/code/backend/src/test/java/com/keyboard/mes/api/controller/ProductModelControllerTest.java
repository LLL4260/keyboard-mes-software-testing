package com.keyboard.mes.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyboard.mes.entity.ProductModel;
import com.keyboard.mes.controller.ProductModelController;
import com.keyboard.mes.service.ProductModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Tag("api")
class ProductModelControllerTest {

    @Mock
    private ProductModelService productModelService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ProductModelController controller = new ProductModelController();
        ReflectionTestUtils.setField(controller, "productModelService", productModelService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProductModelShouldReturnRecord() throws Exception {
        ProductModel productModel = productModel("KB-001", "Hot Swap Keyboard");
        productModel.setId(1L);
        when(productModelService.getProductModelById(1L)).thenReturn(productModel);

        mockMvc.perform(get("/api/productModel/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.modelCode").value("KB-001"));

        verify(productModelService).getProductModelById(1L);
    }

    @Test
    void saveShouldDeserializeRequestAndReturnSuccess() throws Exception {
        ProductModel productModel = productModel("KB-002", "Low Profile Keyboard");
        when(productModelService.save(argThat(model -> "KB-002".equals(model.getModelCode())))).thenReturn(true);

        mockMvc.perform(post("/api/productModel/add")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productModel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("添加成功"));
    }

    @Test
    void updateShouldUsePathId() throws Exception {
        ProductModel productModel = productModel("KB-003", "Wireless Keyboard");
        when(productModelService.update(argThat(model -> Long.valueOf(3L).equals(model.getId())))).thenReturn(true);

        mockMvc.perform(put("/api/productModel/{id}", 3L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productModel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("更新成功"));
    }

    @Test
    void deleteShouldForwardPathId() throws Exception {
        when(productModelService.delete(4L)).thenReturn(true);

        mockMvc.perform(delete("/api/productModel/{id}", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("删除成功"));

        verify(productModelService).delete(4L);
    }

    private ProductModel productModel(String code, String name) {
        ProductModel model = new ProductModel();
        model.setModelCode(code);
        model.setModelName(name);
        model.setStatus(1);
        return model;
    }
}

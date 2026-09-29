package com.keyboard.mes.api.controller;

import com.keyboard.mes.controller.ProductionOrderController;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.service.ProductionOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Tag("api")
class ProductionOrderControllerTest {

    @Mock
    private ProductionOrderService productionOrderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductionOrderController controller = new ProductionOrderController();
        ReflectionTestUtils.setField(controller, "productionOrderService", productionOrderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listShouldReturnProductionOrders() throws Exception {
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        order.setOrderNo("MO-001");
        when(productionOrderService.productionOrderList()).thenReturn(new ArrayList<>(List.of(order)));
        mockMvc.perform(get("/api/productionOrder/list"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].orderNo").value("MO-001"));
    }

    @Test
    void generateTasksShouldReturnGeneratedTaskList() throws Exception {
        ProductionTask task = new ProductionTask();
        task.setId(8L);
        task.setTaskNo("TASK-MO-001-010");
        when(productionOrderService.generateTasks(1L)).thenReturn(new ArrayList<>(List.of(task)));
        mockMvc.perform(post("/api/productionOrder/{id}/generateTasks", 1L))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].taskNo").value("TASK-MO-001-010"));
        verify(productionOrderService).generateTasks(1L);
    }
}

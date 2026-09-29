package com.keyboard.mes.api.controller;

import com.keyboard.mes.controller.ProductionTaskController;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.service.ProductionTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Tag("api")
class ProductionTaskControllerTest {

    @Mock
    private ProductionTaskService productionTaskService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductionTaskController controller = new ProductionTaskController();
        ReflectionTestUtils.setField(controller, "productionTaskService", productionTaskService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void startShouldReturnRunningTask() throws Exception {
        ProductionTask task = task(1);
        when(productionTaskService.start(5L)).thenReturn(task);
        mockMvc.perform(post("/api/productionTask/{id}/start", 5L))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));
        verify(productionTaskService).start(5L);
    }

    @Test
    void pauseShouldReturnPausedTask() throws Exception {
        when(productionTaskService.pause(5L)).thenReturn(task(2));
        mockMvc.perform(post("/api/productionTask/{id}/pause", 5L))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    void finishShouldReturnCompletedTask() throws Exception {
        when(productionTaskService.finish(5L)).thenReturn(task(3));
        mockMvc.perform(post("/api/productionTask/{id}/finish", 5L))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value(3));
    }

    @Test
    void addShouldRejectInvalidTaskBody() throws Exception {
        mockMvc.perform(post("/api/productionTask/add").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    private ProductionTask task(int status) {
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setTaskNo("TASK-005");
        task.setStatus(status);
        return task;
    }
}

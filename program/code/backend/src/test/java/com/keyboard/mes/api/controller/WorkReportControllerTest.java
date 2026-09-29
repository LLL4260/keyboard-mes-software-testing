package com.keyboard.mes.api.controller;

import com.keyboard.mes.service.WorkReportService;
import com.keyboard.mes.controller.WorkReportController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Tag("api")
class WorkReportControllerTest {

    @Mock
    private WorkReportService workReportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WorkReportController controller = new WorkReportController();
        ReflectionTestUtils.setField(controller, "workReportService", workReportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void reportOptionsShouldExposeNumbersAndInternalIds() throws Exception {
        when(workReportService.reportOptions()).thenReturn(List.of(Map.of(
                "taskId", 92L,
                "taskNo", "TASK-0717-012-010",
                "orderId", 12L,
                "orderNo", "MO-20260717-001",
                "stationCode", "ST-PCBA-05",
                "remainingQuantity", 45
        )));

        mockMvc.perform(get("/api/workReport/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].taskNo").value("TASK-0717-012-010"))
                .andExpect(jsonPath("$.data[0].orderNo").value("MO-20260717-001"))
                .andExpect(jsonPath("$.data[0].taskId").value(92))
                .andExpect(jsonPath("$.data[0].orderId").value(12))
                .andExpect(jsonPath("$.data[0].remainingQuantity").value(45));

        verify(workReportService).reportOptions();
    }
}

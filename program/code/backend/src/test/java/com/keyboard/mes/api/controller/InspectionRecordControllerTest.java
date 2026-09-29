package com.keyboard.mes.api.controller;

import com.keyboard.mes.service.InspectionRecordService;
import com.keyboard.mes.controller.InspectionRecordController;
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
class InspectionRecordControllerTest {

    @Mock
    private InspectionRecordService inspectionRecordService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InspectionRecordController controller = new InspectionRecordController();
        ReflectionTestUtils.setField(controller, "inspectionRecordService", inspectionRecordService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void inspectionOptionsShouldExposeBusinessNumbersAndInternalIds() throws Exception {
        when(inspectionRecordService.inspectionOptions()).thenReturn(List.of(Map.of(
                "taskId", 5L,
                "taskNo", "TASK-0717-001-010",
                "orderId", 1L,
                "orderNo", "MO-20260717-001",
                "stationCode", "ST-FQC-01"
        )));

        mockMvc.perform(get("/api/inspectionRecord/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].taskNo").value("TASK-0717-001-010"))
                .andExpect(jsonPath("$.data[0].orderNo").value("MO-20260717-001"))
                .andExpect(jsonPath("$.data[0].taskId").value(5))
                .andExpect(jsonPath("$.data[0].orderId").value(1));

        verify(inspectionRecordService).inspectionOptions();
    }
}

package com.keyboard.mes.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyboard.mes.controller.ReworkOrderController;
import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.service.ReworkOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Tag("api")
class ReworkOrderControllerTest {

    @Mock
    private ReworkOrderService reworkOrderService;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        ReworkOrderController controller = new ReworkOrderController();
        ReflectionTestUtils.setField(controller, "reworkOrderService", reworkOrderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void repairShouldForwardRepairAction() throws Exception {
        ReworkOrder result = new ReworkOrder();
        result.setId(9L);
        result.setStatus(2);
        when(reworkOrderService.repair(org.mockito.ArgumentMatchers.eq(9L),
                argThat(input -> "重新焊接".equals(input.getRepairAction())))).thenReturn(result);
        mockMvc.perform(post("/api/reworkOrder/{id}/repair", 9L)
                        .contentType("application/json").content("{\"repairAction\":\"重新焊接\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    void recheckShouldAcceptValidInspectionPayload() throws Exception {
        InspectionRecord inspection = new InspectionRecord();
        inspection.setInspectionNo("RQ-001");
        inspection.setInspectionType("recheck");
        inspection.setOrderId(1L);
        inspection.setSourceType("manual");
        inspection.setInspectionTime(LocalDateTime.of(2026, 7, 18, 10, 0));
        inspection.setResult(1);
        inspection.setApprovalStatus(0);
        ReworkOrder result = new ReworkOrder();
        result.setId(9L);
        result.setStatus(3);
        when(reworkOrderService.recheck(org.mockito.ArgumentMatchers.eq(9L),
                org.mockito.ArgumentMatchers.any(InspectionRecord.class))).thenReturn(result);
        mockMvc.perform(post("/api/reworkOrder/{id}/recheck", 9L)
                        .contentType("application/json").content(objectMapper.writeValueAsString(inspection)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value(3));
    }
}

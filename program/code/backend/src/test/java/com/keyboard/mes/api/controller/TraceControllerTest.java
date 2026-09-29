package com.keyboard.mes.api.controller;

import com.keyboard.mes.dto.TraceResult;
import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.WorkReport;
import com.keyboard.mes.service.TraceService;
import com.keyboard.mes.controller.TraceController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Tag("api")
class TraceControllerTest {

    @Mock
    private TraceService traceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TraceController(traceService)).build();
    }

    @Test
    void traceShouldReturnRecordsForProductSn() throws Exception {
        TraceResult traceResult = new TraceResult();
        traceResult.setProductSn("SN-ALPHA87-001");
        traceResult.setReports(List.of(new WorkReport()));
        traceResult.setInspections(List.of(new InspectionRecord()));
        traceResult.setReworks(List.of());
        traceResult.setTasks(List.of());
        traceResult.setOrders(List.of());
        when(traceService.getTraceByProductSn("SN-ALPHA87-001")).thenReturn(traceResult);

        mockMvc.perform(get("/api/trace/{productSn}", "SN-ALPHA87-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.productSn").value("SN-ALPHA87-001"))
                .andExpect(jsonPath("$.data.reports.length()").value(1))
                .andExpect(jsonPath("$.data.inspections.length()").value(1));

        verify(traceService).getTraceByProductSn("SN-ALPHA87-001");
    }

    @Test
    void traceShouldReturnEmptyCollectionsWhenNoBusinessRecordsExist() throws Exception {
        TraceResult traceResult = new TraceResult();
        traceResult.setProductSn("SN-NOT-FOUND");
        traceResult.setReports(List.of());
        traceResult.setInspections(List.of());
        traceResult.setReworks(List.of());
        traceResult.setTasks(List.of());
        traceResult.setOrders(List.of());
        when(traceService.getTraceByProductSn("SN-NOT-FOUND")).thenReturn(traceResult);

        mockMvc.perform(get("/api/trace/{productSn}", "SN-NOT-FOUND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reports").isEmpty())
                .andExpect(jsonPath("$.data.orders").isEmpty());
    }
}

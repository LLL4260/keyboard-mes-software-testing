package com.keyboard.mes.api.controller;

import com.keyboard.mes.service.ReportService;
import com.keyboard.mes.controller.ReportController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService)).build();
    }

    @Test
    void overviewShouldReturnProductionSummary() throws Exception {
        when(reportService.overview()).thenReturn(Map.of(
                "targetQuantity", 1000,
                "reportQuantity", 760,
                "progressRate", 76.0
        ));

        mockMvc.perform(get("/api/report/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.targetQuantity").value(1000))
                .andExpect(jsonPath("$.data.reportQuantity").value(760));

        verify(reportService).overview();
    }

    @Test
    void qualityShouldUseWeekWhenPeriodIsOmitted() throws Exception {
        when(reportService.quality("week", null)).thenReturn(Map.of(
                "period", "week",
                "periodLabel", "本周",
                "trendData", List.of()
        ));

        mockMvc.perform(get("/api/report/quality"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.period").value("week"))
                .andExpect(jsonPath("$.data.periodLabel").value("本周"));

        verify(reportService).quality("week", null);
    }

    @Test
    void qualityShouldForwardDayAndProductModelFilters() throws Exception {
        when(reportService.quality("day", 7L)).thenReturn(Map.of(
                "period", "day",
                "productModelId", 7L,
                "totalQuantity", 30
        ));

        mockMvc.perform(get("/api/report/quality")
                        .param("period", "day")
                        .param("productModelId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.period").value("day"))
                .andExpect(jsonPath("$.data.productModelId").value(7))
                .andExpect(jsonPath("$.data.totalQuantity").value(30));

        verify(reportService).quality("day", 7L);
    }

    @Test
    void orderTrackingShouldForwardPathId() throws Exception {
        when(reportService.orderTracking(42L)).thenReturn(Map.of(
                "targetQuantity", 500,
                "completedQuantity", 320
        ));

        mockMvc.perform(get("/api/report/order/{id}", 42L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.completedQuantity").value(320));

        verify(reportService).orderTracking(42L);
    }
}

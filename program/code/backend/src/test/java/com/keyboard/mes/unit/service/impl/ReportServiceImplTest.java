package com.keyboard.mes.unit.service.impl;

import com.keyboard.mes.entity.ProductModel;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.WorkReport;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.InspectionRecordMapper;
import com.keyboard.mes.repository.ProductModelMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.repository.ReworkOrderMapper;
import com.keyboard.mes.repository.WorkReportMapper;
import com.keyboard.mes.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ReportServiceImplTest {

    @Mock
    private ProductionOrderMapper productionOrderMapper;
    @Mock
    private ProductModelMapper productModelMapper;
    @Mock
    private ProductionTaskMapper productionTaskMapper;
    @Mock
    private WorkReportMapper workReportMapper;
    @Mock
    private InspectionRecordMapper inspectionRecordMapper;
    @Mock
    private ReworkOrderMapper reworkOrderMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    private ProductModel alpha;
    private ProductModel pro;

    @BeforeEach
    void setUp() {
        alpha = productModel(1L, "KB-ALPHA-87", "Alpha 87");
        pro = productModel(2L, "KB-PRO-104", "Pro 104");
    }

    private void stubCommonData() {
        when(productModelMapper.productModelList()).thenReturn(new ArrayList<>(List.of(alpha, pro)));
        when(productionOrderMapper.productionOrderList()).thenReturn(new ArrayList<>(List.of(
                productionOrder(10L, 1L),
                productionOrder(20L, 2L)
        )));
        when(inspectionRecordMapper.inspectionRecordList()).thenReturn(new ArrayList<>());
    }

    @Test
    void qualityAggregatesCurrentWeekAcrossAllModels() {
        stubCommonData();
        LocalDateTime today = LocalDate.now().atTime(9, 0);
        when(workReportMapper.workReportList()).thenReturn(new ArrayList<>(List.of(
                workReport(10L, today, 100, 95, 5, 1),
                workReport(20L, today.plusHours(1), 60, 60, 0, 1),
                workReport(10L, today.minusMonths(1), 500, 490, 10, 1),
                workReport(10L, today.plusHours(2), 999, 999, 0, 0)
        )));

        Map<String, Object> result = reportService.quality("week", null);

        assertEquals(160, result.get("totalQuantity"));
        assertEquals(155, result.get("qualifiedQuantity"));
        assertEquals(5, result.get("defectQuantity"));
        assertEquals("全部产品", result.get("productModelName"));
        assertEquals(7, ((List<?>) result.get("trendData")).size());
        assertEquals(2, ((List<?>) result.get("productBreakdown")).size());
    }

    @Test
    void qualityAggregatesCurrentDayOnly() {
        stubCommonData();
        LocalDateTime today = LocalDate.now().atTime(9, 0);
        when(workReportMapper.workReportList()).thenReturn(new ArrayList<>(List.of(
                workReport(10L, today, 75, 72, 3, 1),
                workReport(20L, today.minusDays(1), 40, 40, 0, 1)
        )));

        Map<String, Object> result = reportService.quality("day", null);

        assertEquals("本日", result.get("periodLabel"));
        assertEquals(75, result.get("totalQuantity"));
        assertEquals(1, ((List<?>) result.get("trendData")).size());
    }

    @Test
    void qualityFiltersCurrentMonthByProductModel() {
        stubCommonData();
        LocalDateTime today = LocalDate.now().atTime(10, 0);
        when(productModelMapper.getById(1L)).thenReturn(alpha);
        when(workReportMapper.workReportList()).thenReturn(new ArrayList<>(List.of(
                workReport(10L, today, 120, 116, 4, 1),
                workReport(20L, today, 80, 80, 0, 1)
        )));

        Map<String, Object> result = reportService.quality("month", 1L);

        assertEquals(120, result.get("totalQuantity"));
        assertEquals("KB-ALPHA-87 · Alpha 87", result.get("productModelName"));
        assertEquals(1, ((List<?>) result.get("productBreakdown")).size());
    }

    @Test
    void qualityRejectsUnsupportedPeriod() {
        assertThrows(BusinessException.class, () -> reportService.quality("quarter", null));
    }

    private ProductModel productModel(Long id, String code, String name) {
        ProductModel model = new ProductModel();
        model.setId(id);
        model.setModelCode(code);
        model.setModelName(name);
        model.setCategory("键盘");
        model.setStatus(1);
        return model;
    }

    private ProductionOrder productionOrder(Long id, Long productModelId) {
        ProductionOrder order = new ProductionOrder();
        order.setId(id);
        order.setProductModelId(productModelId);
        return order;
    }

    private WorkReport workReport(Long orderId,
                                  LocalDateTime reportTime,
                                  int quantity,
                                  int qualified,
                                  int defect,
                                  int status) {
        WorkReport report = new WorkReport();
        report.setOrderId(orderId);
        report.setReportTime(reportTime);
        report.setReportQuantity(quantity);
        report.setQualifiedQuantity(qualified);
        report.setDefectQuantity(defect);
        report.setDefectReason(defect > 0 ? "焊接不良" : null);
        report.setStatus(status);
        return report;
    }
}

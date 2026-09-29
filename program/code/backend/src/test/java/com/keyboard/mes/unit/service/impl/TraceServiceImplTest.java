package com.keyboard.mes.unit.service.impl;

import com.keyboard.mes.dto.TraceResult;
import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.entity.WorkReport;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.InspectionRecordMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.repository.ReworkOrderMapper;
import com.keyboard.mes.repository.WorkReportMapper;
import com.keyboard.mes.service.impl.TraceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class TraceServiceImplTest {

    @Mock
    private WorkReportMapper workReportMapper;
    @Mock
    private InspectionRecordMapper inspectionRecordMapper;
    @Mock
    private ReworkOrderMapper reworkOrderMapper;
    @Mock
    private ProductionTaskMapper productionTaskMapper;
    @Mock
    private ProductionOrderMapper productionOrderMapper;

    @InjectMocks
    private TraceServiceImpl traceService;

    @Test
    void traceShouldNormalizeSnAndAggregateRelatedTasksAndOrders() {
        WorkReport report = new WorkReport();
        report.setTaskId(10L);
        report.setOrderId(100L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setTaskId(20L);
        inspection.setOrderId(100L);
        ReworkOrder rework = new ReworkOrder();
        rework.setTaskId(20L);
        rework.setOrderId(200L);

        ProductionTask firstTask = new ProductionTask();
        firstTask.setId(10L);
        firstTask.setOrderId(100L);
        ProductionTask secondTask = new ProductionTask();
        secondTask.setId(20L);
        secondTask.setOrderId(200L);
        ProductionTask unrelatedTask = new ProductionTask();
        unrelatedTask.setId(99L);
        unrelatedTask.setOrderId(999L);

        ProductionOrder firstOrder = new ProductionOrder();
        firstOrder.setId(100L);
        ProductionOrder secondOrder = new ProductionOrder();
        secondOrder.setId(200L);
        ProductionOrder unrelatedOrder = new ProductionOrder();
        unrelatedOrder.setId(999L);

        when(workReportMapper.findByProductSn("SN-001")).thenReturn(new ArrayList<>(List.of(report)));
        when(inspectionRecordMapper.findByProductSn("SN-001")).thenReturn(new ArrayList<>(List.of(inspection)));
        when(reworkOrderMapper.findByProductSn("SN-001")).thenReturn(new ArrayList<>(List.of(rework)));
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(firstTask, secondTask, unrelatedTask)));
        when(productionOrderMapper.productionOrderList()).thenReturn(new ArrayList<>(List.of(firstOrder, secondOrder, unrelatedOrder)));

        TraceResult result = traceService.getTraceByProductSn("  SN-001  ");

        assertThat(result.getProductSn()).isEqualTo("SN-001");
        assertThat(result.getReports()).containsExactly(report);
        assertThat(result.getInspections()).containsExactly(inspection);
        assertThat(result.getReworks()).containsExactly(rework);
        assertThat(result.getTasks()).extracting(ProductionTask::getId).containsExactly(10L, 20L);
        assertThat(result.getOrders()).extracting(ProductionOrder::getId).containsExactly(100L, 200L);
        verify(workReportMapper).findByProductSn("SN-001");
        verify(inspectionRecordMapper).findByProductSn("SN-001");
        verify(reworkOrderMapper).findByProductSn("SN-001");
    }

    @Test
    void traceShouldRejectBlankProductSnBeforeQueryingDatabase() {
        assertThatThrownBy(() -> traceService.getTraceByProductSn("   "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请输入产品 SN");

        verifyNoInteractions(workReportMapper, inspectionRecordMapper, reworkOrderMapper,
                productionTaskMapper, productionOrderMapper);
    }

    @Test
    void traceShouldReturnEmptyResultWithoutLoadingTaskAndOrderLists() {
        when(workReportMapper.findByProductSn("SN-EMPTY")).thenReturn(new ArrayList<>());
        when(inspectionRecordMapper.findByProductSn("SN-EMPTY")).thenReturn(new ArrayList<>());
        when(reworkOrderMapper.findByProductSn("SN-EMPTY")).thenReturn(new ArrayList<>());

        TraceResult result = traceService.getTraceByProductSn("SN-EMPTY");

        assertThat(result.getReports()).isEmpty();
        assertThat(result.getInspections()).isEmpty();
        assertThat(result.getReworks()).isEmpty();
        assertThat(result.getTasks()).isEmpty();
        assertThat(result.getOrders()).isEmpty();
        verify(productionTaskMapper, never()).productionTaskList();
        verify(productionOrderMapper, never()).productionOrderList();
    }
}

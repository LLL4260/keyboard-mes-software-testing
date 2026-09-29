package com.keyboard.mes.unit.service.impl;

import com.keyboard.mes.entity.ProcessRoute;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.WorkReport;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.ProcessRouteMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.repository.WorkReportMapper;
import com.keyboard.mes.service.impl.WorkReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class WorkReportServiceImplTest {

    @Mock
    private WorkReportMapper workReportMapper;

    @Mock
    private ProductionTaskMapper productionTaskMapper;

    @Mock
    private ProductionOrderMapper productionOrderMapper;

    @Mock
    private ProcessRouteMapper processRouteMapper;

    @InjectMocks
    private WorkReportServiceImpl workReportService;

    @Test
    void basicOperationsShouldReturnMapperDataAndReportWriteOutcomes() {
        WorkReport report = report(1);
        ArrayList<WorkReport> reports = new ArrayList<>(List.of(report));
        when(workReportMapper.workReportList()).thenReturn(reports);
        when(workReportMapper.getById(5L)).thenReturn(report);
        when(workReportMapper.insert(report)).thenReturn(1, 0);
        when(workReportMapper.delete(5L)).thenReturn(1, 0);
        when(workReportMapper.update(report)).thenReturn(1, 0);

        assertThat(workReportService.workReportList()).isSameAs(reports);
        assertThat(workReportService.getWorkReportById(5L)).isSameAs(report);
        assertThat(workReportService.save(report)).isTrue();
        assertThat(workReportService.save(report)).isFalse();
        assertThat(workReportService.delete(5L)).isTrue();
        assertThat(workReportService.delete(5L)).isFalse();
        assertThat(workReportService.update(report)).isTrue();
        assertThat(workReportService.update(report)).isFalse();
    }

    @Test
    void reportOptionsShouldJoinTaskAndOrderNumbersAndExcludeFinishedTasks() {
        ProductionOrder order = new ProductionOrder();
        order.setId(12L);
        order.setOrderNo("MO-20260717-001");

        ProductionTask runningTask = new ProductionTask();
        runningTask.setId(92L);
        runningTask.setTaskNo("TASK-0717-012-010");
        runningTask.setOrderId(12L);
        runningTask.setStationCode("ST-PCBA-05");
        runningTask.setPlannedQuantity(75);
        runningTask.setCompletedQuantity(30);
        runningTask.setStatus(1);

        ProductionTask finishedTask = new ProductionTask();
        finishedTask.setId(91L);
        finishedTask.setTaskNo("TASK-FINISHED");
        finishedTask.setOrderId(12L);
        finishedTask.setPlannedQuantity(75);
        finishedTask.setCompletedQuantity(75);
        finishedTask.setStatus(3);

        when(productionOrderMapper.productionOrderList()).thenReturn(new ArrayList<>(List.of(order)));
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(finishedTask, runningTask)));

        List<Map<String, Object>> options = workReportService.reportOptions();

        assertThat(options).hasSize(1);
        assertThat(options.get(0)).containsEntry("taskId", 92L)
                .containsEntry("taskNo", "TASK-0717-012-010")
                .containsEntry("orderId", 12L)
                .containsEntry("orderNo", "MO-20260717-001")
                .containsEntry("stationCode", "ST-PCBA-05")
                .containsEntry("remainingQuantity", 45);
    }

    @Test
    void submitShouldUpdateTaskAndOrderWhenQualityGateIsWaitingInspection() {
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setOrderId(1L);
        task.setProcessRouteId(9L);
        task.setStationCode("ST-01");
        task.setPlannedQuantity(10);
        task.setCompletedQuantity(4);
        task.setDefectQuantity(1);
        task.setStatus(1);

        ProductionOrder order = new ProductionOrder();
        order.setId(1L);

        ProcessRoute route = new ProcessRoute();
        route.setId(9L);
        route.setQualityGate(1);

        WorkReport report = new WorkReport();
        report.setTaskId(5L);
        report.setOrderId(1L);
        report.setReportQuantity(6);

        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(processRouteMapper.getById(9L)).thenReturn(route);
        when(workReportMapper.insert(any(WorkReport.class))).thenAnswer(invocation -> {
            WorkReport inserted = invocation.getArgument(0);
            inserted.setId(101L);
            return 1;
        });
        when(productionTaskMapper.update(task)).thenReturn(1);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(task)));
        when(workReportMapper.getById(101L)).thenReturn(report);

        WorkReport saved = workReportService.submit(report);

        assertThat(saved).isSameAs(report);
        assertThat(report.getReportNo()).startsWith("WR-");
        assertThat(report.getStationCode()).isEqualTo("ST-01");
        assertThat(report.getQualifiedQuantity()).isEqualTo(6);
        assertThat(report.getDefectQuantity()).isZero();
        assertThat(report.getStatus()).isEqualTo(1);
        assertThat(task.getCompletedQuantity()).isEqualTo(10);
        assertThat(task.getDefectQuantity()).isEqualTo(1);
        assertThat(task.getStatus()).isEqualTo(4);
        assertThat(order.getStatus()).isEqualTo(3);
        verify(productionOrderMapper).update(order);
    }

    @Test
    void submitPartialReportShouldKeepTaskRunningAndOrderProducing() {
        ProductionTask task = task();
        ProductionOrder order = order();
        WorkReport report = report(3);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(workReportMapper.insert(any())).thenAnswer(invocation -> {
            WorkReport inserted = invocation.getArgument(0);
            inserted.setId(102L);
            return 1;
        });
        when(productionTaskMapper.update(task)).thenReturn(1);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(task)));
        when(workReportMapper.getById(102L)).thenReturn(report);

        workReportService.submit(report);

        assertThat(task.getCompletedQuantity()).isEqualTo(7);
        assertThat(task.getStatus()).isEqualTo(1);
        assertThat(task.getFinishTime()).isNull();
        assertThat(order.getStatus()).isEqualTo(2);
    }

    @Test
    void submitShouldRejectMissingTaskIdBeforeDatabaseMutation() {
        WorkReport report = new WorkReport();
        report.setOrderId(1L);

        assertThatThrownBy(() -> workReportService.submit(report))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生产任务不能为空");
        verifyNoInteractions(workReportMapper, productionOrderMapper, processRouteMapper);
    }

    @Test
    void submitShouldRejectTaskFromDifferentOrder() {
        ProductionTask task = task();
        task.setOrderId(2L);
        ProductionOrder order = order();
        WorkReport report = report(1);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);

        assertThatThrownBy(() -> workReportService.submit(report))
                .isInstanceOf(BusinessException.class)
                .hasMessage("报工任务与工单不匹配");
        verify(workReportMapper, never()).insert(any());
    }

    @Test
    void submitShouldStopWhenReportPersistenceFails() {
        ProductionTask task = task();
        ProductionOrder order = order();
        WorkReport report = report(1);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(workReportMapper.insert(report)).thenReturn(0);

        assertThatThrownBy(() -> workReportService.submit(report))
                .isInstanceOf(BusinessException.class)
                .hasMessage("报工保存失败");
        verify(productionTaskMapper, never()).update(any());
        verify(productionOrderMapper, never()).update(any());
    }

    @Test
    void submitShouldRejectQuantityBoundaryAndConservationViolations() {
        when(productionTaskMapper.getById(5L)).thenReturn(task());
        when(productionOrderMapper.getById(1L)).thenReturn(order());

        assertThatThrownBy(() -> workReportService.submit(report(0)))
                .isInstanceOf(BusinessException.class).hasMessage("报工数量必须大于 0");

        WorkReport negativeQualified = report(2);
        negativeQualified.setQualifiedQuantity(-1);
        negativeQualified.setDefectQuantity(3);
        assertThatThrownBy(() -> workReportService.submit(negativeQualified))
                .isInstanceOf(BusinessException.class).hasMessage("合格数和不良数不得为负");

        WorkReport negativeDefect = report(2);
        negativeDefect.setQualifiedQuantity(3);
        negativeDefect.setDefectQuantity(-1);
        assertThatThrownBy(() -> workReportService.submit(negativeDefect))
                .isInstanceOf(BusinessException.class).hasMessage("合格数和不良数不得为负");

        WorkReport nonConserving = report(2);
        nonConserving.setQualifiedQuantity(1);
        nonConserving.setDefectQuantity(0);
        assertThatThrownBy(() -> workReportService.submit(nonConserving))
                .isInstanceOf(BusinessException.class).hasMessage("合格数与不良数之和必须等于报工数量");

        assertThatThrownBy(() -> workReportService.submit(report(7)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("剩余 6");
        verify(workReportMapper, never()).insert(any());
    }

    @Test
    void submitShouldRejectMissingTaskAndOrderRecords() {
        when(productionTaskMapper.getById(5L)).thenReturn(null);
        assertThatThrownBy(() -> workReportService.submit(report(1)))
                .isInstanceOf(BusinessException.class).hasMessage("生产任务不存在");

        when(productionTaskMapper.getById(5L)).thenReturn(task());
        WorkReport noOrderId = report(1);
        noOrderId.setOrderId(null);
        assertThatThrownBy(() -> workReportService.submit(noOrderId))
                .isInstanceOf(BusinessException.class).hasMessage("生产工单不能为空");

        assertThatThrownBy(() -> workReportService.submit(report(1)))
                .isInstanceOf(BusinessException.class).hasMessage("生产工单不存在");
        verify(workReportMapper, never()).insert(any());
    }

    @Test
    void submitExactRemainingWithoutQualityGateShouldCompleteTaskAndOrder() {
        ProductionTask task = task();
        LocalDateTime originalStart = LocalDateTime.of(2026, 7, 18, 8, 0);
        task.setStartTime(originalStart);
        ProductionOrder order = order();
        WorkReport report = report(6);
        report.setReportNo("WR-EXPLICIT");
        report.setReportTime(LocalDateTime.of(2026, 7, 18, 9, 0));
        report.setStatus(1);
        report.setStationCode("ST-EXPLICIT");
        report.setQualifiedQuantity(5);
        report.setDefectQuantity(1);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(workReportMapper.insert(report)).thenAnswer(invocation -> {
            report.setId(103L);
            return 1;
        });
        when(productionTaskMapper.update(task)).thenReturn(1);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(task)));
        when(workReportMapper.getById(103L)).thenReturn(report);

        assertThat(workReportService.submit(report)).isSameAs(report);
        assertThat(task.getCompletedQuantity()).isEqualTo(10);
        assertThat(task.getDefectQuantity()).isEqualTo(1);
        assertThat(task.getStartTime()).isEqualTo(originalStart);
        assertThat(task.getStatus()).isEqualTo(3);
        assertThat(order.getStatus()).isEqualTo(4);
        assertThat(report.getReportNo()).isEqualTo("WR-EXPLICIT");
        assertThat(report.getStationCode()).isEqualTo("ST-EXPLICIT");
    }

    @Test
    void submitShouldReportTaskProgressUpdateFailure() {
        WorkReport report = report(1);
        ProductionTask task = task();
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order());
        when(workReportMapper.insert(report)).thenReturn(1);
        when(productionTaskMapper.update(task)).thenReturn(0);

        assertThatThrownBy(() -> workReportService.submit(report))
                .isInstanceOf(BusinessException.class).hasMessage("生产任务进度更新失败");
        verify(productionOrderMapper, never()).update(any());
    }

    private ProductionTask task() {
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setOrderId(1L);
        task.setStationCode("ST-01");
        task.setPlannedQuantity(10);
        task.setCompletedQuantity(4);
        task.setDefectQuantity(0);
        task.setStatus(1);
        return task;
    }

    private ProductionOrder order() {
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        return order;
    }

    private WorkReport report(int quantity) {
        WorkReport report = new WorkReport();
        report.setTaskId(5L);
        report.setOrderId(1L);
        report.setReportQuantity(quantity);
        return report;
    }
}

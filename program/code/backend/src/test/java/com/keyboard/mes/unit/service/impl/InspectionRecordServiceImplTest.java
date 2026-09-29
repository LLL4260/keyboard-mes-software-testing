package com.keyboard.mes.unit.service.impl;

import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.InspectionRecordMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.repository.ReworkOrderMapper;
import com.keyboard.mes.service.impl.InspectionRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class InspectionRecordServiceImplTest {

    @Mock
    private InspectionRecordMapper inspectionRecordMapper;

    @Mock
    private ProductionTaskMapper productionTaskMapper;

    @Mock
    private ProductionOrderMapper productionOrderMapper;

    @Mock
    private ReworkOrderMapper reworkOrderMapper;

    @InjectMocks
    private InspectionRecordServiceImpl inspectionRecordService;

    @Test
    void basicOperationsShouldReturnMapperDataAndReportWriteOutcomes() {
        InspectionRecord inspection = new InspectionRecord();
        ArrayList<InspectionRecord> records = new ArrayList<>(List.of(inspection));
        when(inspectionRecordMapper.inspectionRecordList()).thenReturn(records);
        when(inspectionRecordMapper.getById(1L)).thenReturn(inspection);
        when(inspectionRecordMapper.insert(inspection)).thenReturn(1, 0);
        when(inspectionRecordMapper.delete(1L)).thenReturn(1, 0);
        when(inspectionRecordMapper.update(inspection)).thenReturn(1, 0);

        assertThat(inspectionRecordService.inspectionRecordList()).isSameAs(records);
        assertThat(inspectionRecordService.getInspectionRecordById(1L)).isSameAs(inspection);
        assertThat(inspectionRecordService.save(inspection)).isTrue();
        assertThat(inspectionRecordService.save(inspection)).isFalse();
        assertThat(inspectionRecordService.delete(1L)).isTrue();
        assertThat(inspectionRecordService.delete(1L)).isFalse();
        assertThat(inspectionRecordService.update(inspection)).isTrue();
        assertThat(inspectionRecordService.update(inspection)).isFalse();
    }

    @Test
    void inspectionOptionsShouldExposeNumbersAndExcludeRunningTasks() {
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        order.setOrderNo("MO-20260717-001");

        ProductionTask waiting = new ProductionTask();
        waiting.setId(5L);
        waiting.setTaskNo("TASK-0717-001-010");
        waiting.setOrderId(1L);
        waiting.setStationCode("ST-FQC-01");
        waiting.setStatus(4);

        ProductionTask running = new ProductionTask();
        running.setId(6L);
        running.setTaskNo("TASK-0717-001-020");
        running.setOrderId(1L);
        running.setStatus(1);

        when(productionOrderMapper.productionOrderList()).thenReturn(new ArrayList<>(List.of(order)));
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(running, waiting)));

        List<Map<String, Object>> options = inspectionRecordService.inspectionOptions();

        assertThat(options).hasSize(1);
        assertThat(options.get(0)).containsEntry("taskId", 5L)
                .containsEntry("taskNo", "TASK-0717-001-010")
                .containsEntry("orderId", 1L)
                .containsEntry("orderNo", "MO-20260717-001")
                .containsEntry("stationCode", "ST-FQC-01");
    }

    @Test
    void submitFailedInspectionShouldCreateReworkAndMarkTaskAbnormal() {
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setOrderId(1L);
        task.setStatus(4);

        ProductionOrder order = new ProductionOrder();
        order.setId(1L);

        InspectionRecord inspection = new InspectionRecord();
        inspection.setInspectionNo("QC-001");
        inspection.setTaskId(5L);
        inspection.setOrderId(1L);
        inspection.setProductSn("SN-001");
        inspection.setResult(0);
        inspection.setHandlingMethod(1);
        inspection.setDefectReason("Key noise");

        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(inspectionRecordMapper.insert(any(InspectionRecord.class))).thenAnswer(invocation -> {
            InspectionRecord inserted = invocation.getArgument(0);
            inserted.setId(201L);
            return 1;
        });
        when(reworkOrderMapper.insert(any(ReworkOrder.class))).thenReturn(1);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(task)));
        when(inspectionRecordMapper.getById(201L)).thenReturn(inspection);

        InspectionRecord saved = inspectionRecordService.submit(inspection);

        assertThat(saved).isSameAs(inspection);
        assertThat(inspection.getInspectionType()).isEqualTo("process");
        assertThat(inspection.getSourceType()).isEqualTo("manual");
        assertThat(inspection.getApprovalStatus()).isZero();
        assertThat(task.getStatus()).isEqualTo(5);
        assertThat(order.getStatus()).isEqualTo(2);

        ArgumentCaptor<ReworkOrder> reworkCaptor = ArgumentCaptor.forClass(ReworkOrder.class);
        verify(reworkOrderMapper).insert(reworkCaptor.capture());
        ReworkOrder rework = reworkCaptor.getValue();
        assertThat(rework.getReworkNo()).isEqualTo("RW-QC-001");
        assertThat(rework.getSourceInspectionId()).isEqualTo(201L);
        assertThat(rework.getOrderId()).isEqualTo(1L);
        assertThat(rework.getTaskId()).isEqualTo(5L);
        assertThat(rework.getProductSn()).isEqualTo("SN-001");
        assertThat(rework.getStatus()).isZero();
        verify(productionOrderMapper).update(order);
    }

    @Test
    void submitPassedInspectionShouldFinishTaskAndCompleteOrder() {
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setOrderId(1L);
        task.setStatus(4);
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setTaskId(5L);
        inspection.setOrderId(1L);
        inspection.setResult(1);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(inspectionRecordMapper.insert(any())).thenAnswer(invocation -> {
            InspectionRecord saved = invocation.getArgument(0);
            saved.setId(202L);
            return 1;
        });
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(task)));
        when(inspectionRecordMapper.getById(202L)).thenReturn(inspection);

        inspectionRecordService.submit(inspection);

        assertThat(task.getStatus()).isEqualTo(3);
        assertThat(task.getFinishTime()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(4);
        verify(reworkOrderMapper, never()).insert(any());
        verify(productionOrderMapper).update(order);
    }

    @Test
    void submitShouldRejectTaskFromDifferentOrder() {
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setOrderId(2L);
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setTaskId(5L);
        inspection.setOrderId(1L);
        inspection.setResult(1);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);

        assertThatThrownBy(() -> inspectionRecordService.submit(inspection))
                .isInstanceOf(BusinessException.class)
                .hasMessage("质检任务与工单不匹配");
        verify(inspectionRecordMapper, never()).insert(any());
    }

    @Test
    void submitShouldReportAutomaticReworkCreationFailure() {
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setOrderId(1L);
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setTaskId(5L);
        inspection.setOrderId(1L);
        inspection.setResult(0);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(inspectionRecordMapper.insert(any())).thenAnswer(invocation -> {
            InspectionRecord saved = invocation.getArgument(0);
            saved.setId(203L);
            return 1;
        });
        when(reworkOrderMapper.insert(any())).thenReturn(0);

        assertThatThrownBy(() -> inspectionRecordService.submit(inspection))
                .isInstanceOf(BusinessException.class)
                .hasMessage("自动生成返修单失败");
        verify(productionTaskMapper, never()).update(any());
        verify(productionOrderMapper, never()).update(any());
    }

    @Test
    void submitShouldRejectMissingTaskAndOrderBeforeSaving() {
        InspectionRecord noTask = new InspectionRecord();
        noTask.setTaskId(5L);
        noTask.setOrderId(1L);
        assertThatThrownBy(() -> inspectionRecordService.submit(noTask))
                .isInstanceOf(BusinessException.class).hasMessage("生产任务不存在");

        InspectionRecord noOrderId = new InspectionRecord();
        assertThatThrownBy(() -> inspectionRecordService.submit(noOrderId))
                .isInstanceOf(BusinessException.class).hasMessage("生产工单不能为空");

        InspectionRecord missingOrder = new InspectionRecord();
        missingOrder.setOrderId(1L);
        assertThatThrownBy(() -> inspectionRecordService.submit(missingOrder))
                .isInstanceOf(BusinessException.class).hasMessage("生产工单不存在");
        verify(inspectionRecordMapper, never()).insert(any());
    }

    @Test
    void submitWithoutTaskShouldKeepOrderUnchangedWhenTaskListIsEmpty() {
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setOrderId(1L);
        inspection.setResult(1);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(inspectionRecordMapper.insert(inspection)).thenAnswer(invocation -> {
            inspection.setId(204L);
            return 1;
        });
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>());
        when(inspectionRecordMapper.getById(204L)).thenReturn(inspection);

        assertThat(inspectionRecordService.submit(inspection)).isSameAs(inspection);
        verify(productionTaskMapper, never()).update(any());
        verify(reworkOrderMapper, never()).insert(any());
        verify(productionOrderMapper, never()).update(any());
    }

    @Test
    void submitWithReworkHandlingShouldCreateReworkEvenWhenResultPasses() {
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setOrderId(1L);
        inspection.setResult(1);
        inspection.setHandlingMethod(1);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(inspectionRecordMapper.insert(inspection)).thenAnswer(invocation -> {
            inspection.setId(205L);
            return 1;
        });
        when(reworkOrderMapper.insert(any())).thenReturn(1);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>());
        when(inspectionRecordMapper.getById(205L)).thenReturn(inspection);

        inspectionRecordService.submit(inspection);

        verify(reworkOrderMapper).insert(any());
        verify(productionTaskMapper, never()).update(any());
    }

    @Test
    void submitShouldPreserveExplicitFieldsAndWaitingInspectionOrderStatus() {
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setOrderId(1L);
        task.setStatus(4);
        LocalDateTime finishTime = LocalDateTime.of(2026, 7, 18, 9, 0);
        task.setFinishTime(finishTime);
        ProductionTask waitingTask = new ProductionTask();
        waitingTask.setId(6L);
        waitingTask.setOrderId(1L);
        waitingTask.setStatus(4);
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setTaskId(5L);
        inspection.setOrderId(1L);
        inspection.setResult(1);
        inspection.setInspectionNo("QC-EXPLICIT");
        inspection.setInspectionType("final");
        inspection.setSourceType("device");
        inspection.setInspectionTime(LocalDateTime.of(2026, 7, 18, 10, 0));
        inspection.setApprovalStatus(1);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(inspectionRecordMapper.insert(inspection)).thenAnswer(invocation -> {
            inspection.setId(206L);
            return 1;
        });
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(task, waitingTask)));
        when(inspectionRecordMapper.getById(206L)).thenReturn(inspection);

        inspectionRecordService.submit(inspection);

        assertThat(task.getStatus()).isEqualTo(3);
        assertThat(task.getFinishTime()).isEqualTo(finishTime);
        assertThat(order.getStatus()).isEqualTo(3);
        assertThat(inspection.getInspectionNo()).isEqualTo("QC-EXPLICIT");
        assertThat(inspection.getInspectionType()).isEqualTo("final");
        assertThat(inspection.getSourceType()).isEqualTo("device");
        assertThat(inspection.getApprovalStatus()).isEqualTo(1);
    }

    @Test
    void submitShouldStopWhenInspectionCannotBeSaved() {
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setOrderId(1L);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(inspectionRecordMapper.insert(inspection)).thenReturn(0);

        assertThatThrownBy(() -> inspectionRecordService.submit(inspection))
                .isInstanceOf(BusinessException.class).hasMessage("质检记录保存失败");
        verify(productionTaskMapper, never()).update(any());
        verify(reworkOrderMapper, never()).insert(any());
    }
}

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
import com.keyboard.mes.service.impl.ReworkOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
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
class ReworkOrderServiceImplTest {

    @Mock
    private ReworkOrderMapper reworkOrderMapper;

    @Mock
    private InspectionRecordMapper inspectionRecordMapper;

    @Mock
    private ProductionTaskMapper productionTaskMapper;

    @Mock
    private ProductionOrderMapper productionOrderMapper;

    @InjectMocks
    private ReworkOrderServiceImpl reworkOrderService;

    @Test
    void basicOperationsShouldReturnMapperDataAndReportWriteOutcomes() {
        ReworkOrder rework = rework();
        ArrayList<ReworkOrder> records = new ArrayList<>(List.of(rework));
        when(reworkOrderMapper.reworkOrderList()).thenReturn(records);
        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(reworkOrderMapper.insert(rework)).thenReturn(1, 0);
        when(reworkOrderMapper.delete(9L)).thenReturn(1, 0);
        when(reworkOrderMapper.update(rework)).thenReturn(1, 0);

        assertThat(reworkOrderService.reworkOrderList()).isSameAs(records);
        assertThat(reworkOrderService.getReworkOrderById(9L)).isSameAs(rework);
        assertThat(reworkOrderService.save(rework)).isTrue();
        assertThat(reworkOrderService.save(rework)).isFalse();
        assertThat(reworkOrderService.delete(9L)).isTrue();
        assertThat(reworkOrderService.delete(9L)).isFalse();
        assertThat(reworkOrderService.update(rework)).isTrue();
        assertThat(reworkOrderService.update(rework)).isFalse();
    }

    @Test
    void recheckPassedShouldCloseReworkAndFinishRelatedTaskAndOrder() {
        ReworkOrder rework = new ReworkOrder();
        rework.setId(9L);
        rework.setOrderId(1L);
        rework.setTaskId(5L);
        rework.setProductSn("SN-001");
        rework.setStatus(2);

        ProductionTask task = new ProductionTask();
        task.setId(5L);
        task.setOrderId(1L);
        task.setStatus(5);

        ProductionOrder order = new ProductionOrder();
        order.setId(1L);

        InspectionRecord inspection = new InspectionRecord();
        inspection.setResult(1);

        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(inspectionRecordMapper.insert(any(InspectionRecord.class))).thenAnswer(invocation -> {
            InspectionRecord inserted = invocation.getArgument(0);
            inserted.setId(301L);
            return 1;
        });
        when(reworkOrderMapper.update(rework)).thenReturn(1);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(1);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(task)));

        ReworkOrder saved = reworkOrderService.recheck(9L, inspection);

        assertThat(saved).isSameAs(rework);
        assertThat(rework.getRecheckInspectionId()).isEqualTo(301L);
        assertThat(rework.getStatus()).isEqualTo(3);
        assertThat(task.getStatus()).isEqualTo(3);
        assertThat(task.getFinishTime()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(4);

        ArgumentCaptor<InspectionRecord> inspectionCaptor = ArgumentCaptor.forClass(InspectionRecord.class);
        verify(inspectionRecordMapper).insert(inspectionCaptor.capture());
        InspectionRecord savedInspection = inspectionCaptor.getValue();
        assertThat(savedInspection.getInspectionNo()).startsWith("RQ-");
        assertThat(savedInspection.getInspectionType()).isEqualTo("recheck");
        assertThat(savedInspection.getTaskId()).isEqualTo(5L);
        assertThat(savedInspection.getOrderId()).isEqualTo(1L);
        assertThat(savedInspection.getProductSn()).isEqualTo("SN-001");
        assertThat(savedInspection.getSourceType()).isEqualTo("manual");
        assertThat(savedInspection.getApprovalStatus()).isZero();
        verify(productionOrderMapper).update(order);
    }

    @Test
    void repairShouldMergeInputAndDefaultToWaitingRecheck() {
        ReworkOrder rework = rework();
        ReworkOrder input = new ReworkOrder();
        input.setAssigneeId(7L);
        input.setRepairAction("重新焊接");
        input.setReplacedMaterial("PCB");
        input.setRepairHours(new BigDecimal("1.5"));
        input.setRepairResult("已修复");
        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(reworkOrderMapper.update(rework)).thenReturn(1);

        ReworkOrder result = reworkOrderService.repair(9L, input);

        assertThat(result).isSameAs(rework);
        assertThat(rework.getAssigneeId()).isEqualTo(7L);
        assertThat(rework.getRepairAction()).isEqualTo("重新焊接");
        assertThat(rework.getRepairTime()).isNotNull();
        assertThat(rework.getStatus()).isEqualTo(2);
    }

    @Test
    void repairShouldPreserveExplicitTimeAndStatus() {
        ReworkOrder rework = rework();
        ReworkOrder input = new ReworkOrder();
        LocalDateTime repairTime = LocalDateTime.of(2026, 7, 18, 12, 0);
        input.setRepairTime(repairTime);
        input.setStatus(1);
        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(reworkOrderMapper.update(rework)).thenReturn(1);

        reworkOrderService.repair(9L, input);

        assertThat(rework.getRepairTime()).isEqualTo(repairTime);
        assertThat(rework.getStatus()).isEqualTo(1);
    }

    @Test
    void recheckFailedShouldReturnToRepairWithoutClosingTaskOrOrder() {
        ReworkOrder rework = rework();
        InspectionRecord inspection = new InspectionRecord();
        inspection.setResult(0);
        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(inspectionRecordMapper.insert(any())).thenAnswer(invocation -> {
            InspectionRecord saved = invocation.getArgument(0);
            saved.setId(302L);
            return 1;
        });
        when(reworkOrderMapper.update(rework)).thenReturn(1);

        reworkOrderService.recheck(9L, inspection);

        assertThat(rework.getStatus()).isEqualTo(1);
        assertThat(rework.getRecheckInspectionId()).isEqualTo(302L);
        verifyNoInteractions(productionTaskMapper, productionOrderMapper);
    }

    @Test
    void recheckShouldRejectNullReworkIdBeforePersistence() {
        InspectionRecord inspection = new InspectionRecord();

        assertThatThrownBy(() -> reworkOrderService.recheck(null, inspection))
                .isInstanceOf(BusinessException.class)
                .hasMessage("返修单不能为空");
        verifyNoInteractions(inspectionRecordMapper, productionTaskMapper, productionOrderMapper);
    }

    @Test
    void recheckShouldReportInspectionInsertFailure() {
        ReworkOrder rework = rework();
        InspectionRecord inspection = new InspectionRecord();
        inspection.setResult(1);
        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(inspectionRecordMapper.insert(any())).thenReturn(0);

        assertThatThrownBy(() -> reworkOrderService.recheck(9L, inspection))
                .isInstanceOf(BusinessException.class)
                .hasMessage("复检记录保存失败");
        verify(reworkOrderMapper, never()).update(rework);
        verifyNoInteractions(productionTaskMapper, productionOrderMapper);
    }

    @Test
    void recheckShouldRejectMissingReworkAndFailedReworkUpdate() {
        ReworkOrder rework = rework();
        when(reworkOrderMapper.getById(9L)).thenReturn(null, rework);
        assertThatThrownBy(() -> reworkOrderService.recheck(9L, new InspectionRecord()))
                .isInstanceOf(BusinessException.class).hasMessage("返修单不存在");

        InspectionRecord inspection = new InspectionRecord();
        inspection.setResult(1);
        when(inspectionRecordMapper.insert(inspection)).thenReturn(1);
        when(reworkOrderMapper.update(rework)).thenReturn(0);
        assertThatThrownBy(() -> reworkOrderService.recheck(9L, inspection))
                .isInstanceOf(BusinessException.class).hasMessage("返修单更新失败");
        verifyNoInteractions(productionTaskMapper, productionOrderMapper);
    }

    @Test
    void recheckShouldPreserveExplicitInspectionFieldsWithoutRelatedTaskOrOrder() {
        ReworkOrder rework = rework();
        rework.setTaskId(null);
        rework.setOrderId(null);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setResult(1);
        inspection.setInspectionNo("RQ-EXPLICIT");
        inspection.setSourceType("device");
        LocalDateTime inspectionTime = LocalDateTime.of(2026, 7, 18, 10, 0);
        inspection.setInspectionTime(inspectionTime);
        inspection.setApprovalStatus(1);
        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(inspectionRecordMapper.insert(inspection)).thenAnswer(invocation -> {
            inspection.setId(303L);
            return 1;
        });
        when(reworkOrderMapper.update(rework)).thenReturn(1);

        reworkOrderService.recheck(9L, inspection);

        assertThat(inspection.getInspectionNo()).isEqualTo("RQ-EXPLICIT");
        assertThat(inspection.getSourceType()).isEqualTo("device");
        assertThat(inspection.getInspectionTime()).isEqualTo(inspectionTime);
        assertThat(inspection.getApprovalStatus()).isEqualTo(1);
        assertThat(rework.getStatus()).isEqualTo(3);
        verifyNoInteractions(productionTaskMapper, productionOrderMapper);
    }

    @Test
    void recheckPassedShouldTolerateMissingRelatedTaskAndOrder() {
        ReworkOrder rework = rework();
        InspectionRecord inspection = new InspectionRecord();
        inspection.setResult(1);
        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(inspectionRecordMapper.insert(inspection)).thenReturn(1);
        when(reworkOrderMapper.update(rework)).thenReturn(1);

        reworkOrderService.recheck(9L, inspection);

        assertThat(rework.getStatus()).isEqualTo(3);
        verify(productionTaskMapper).getById(5L);
        verify(productionOrderMapper).getById(1L);
        verify(productionTaskMapper, never()).update(any());
        verify(productionOrderMapper, never()).update(any());
    }

    @Test
    void recheckPassedShouldReportRelatedTaskUpdateFailure() {
        ReworkOrder rework = rework();
        ProductionTask task = new ProductionTask();
        task.setId(5L);
        InspectionRecord inspection = new InspectionRecord();
        inspection.setResult(1);
        when(reworkOrderMapper.getById(9L)).thenReturn(rework);
        when(inspectionRecordMapper.insert(inspection)).thenReturn(1);
        when(reworkOrderMapper.update(rework)).thenReturn(1);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(0);

        assertThatThrownBy(() -> reworkOrderService.recheck(9L, inspection))
                .isInstanceOf(BusinessException.class).hasMessage("返修复检后任务状态更新失败");
        verify(productionOrderMapper, never()).update(any());
    }

    private ReworkOrder rework() {
        ReworkOrder rework = new ReworkOrder();
        rework.setId(9L);
        rework.setOrderId(1L);
        rework.setTaskId(5L);
        rework.setProductSn("SN-001");
        rework.setStatus(2);
        return rework;
    }
}

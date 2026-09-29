package com.keyboard.mes.unit.service.impl;

import com.keyboard.mes.entity.ProcessRoute;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.ProcessRouteMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.service.impl.ProductionTaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ProductionTaskServiceImplTest {

    @Mock
    private ProductionTaskMapper productionTaskMapper;

    @Mock
    private ProcessRouteMapper processRouteMapper;

    @InjectMocks
    private ProductionTaskServiceImpl productionTaskService;

    @Test
    void basicOperationsShouldReturnMapperDataAndReportWriteOutcomes() {
        ProductionTask task = task(1L);
        ArrayList<ProductionTask> tasks = new ArrayList<>();
        tasks.add(task);
        when(productionTaskMapper.productionTaskList()).thenReturn(tasks);
        when(productionTaskMapper.getById(1L)).thenReturn(task);
        when(productionTaskMapper.insert(task)).thenReturn(1, 0);
        when(productionTaskMapper.delete(1L)).thenReturn(1, 0);
        when(productionTaskMapper.update(task)).thenReturn(1, 0);

        assertThat(productionTaskService.productionTaskList()).isSameAs(tasks);
        assertThat(productionTaskService.getProductionTaskById(1L)).isSameAs(task);
        assertThat(productionTaskService.save(task)).isTrue();
        assertThat(productionTaskService.save(task)).isFalse();
        assertThat(productionTaskService.delete(1L)).isTrue();
        assertThat(productionTaskService.delete(1L)).isFalse();
        assertThat(productionTaskService.update(task)).isTrue();
        assertThat(productionTaskService.update(task)).isFalse();
    }

    @Test
    void startShouldSetRunningStatusAndStartTime() {
        ProductionTask task = task(1L);
        when(productionTaskMapper.getById(1L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(1);

        ProductionTask result = productionTaskService.start(1L);

        assertThat(result).isSameAs(task);
        assertThat(task.getStatus()).isEqualTo(1);
        assertThat(task.getStartTime()).isNotNull();
        verify(productionTaskMapper).update(task);
    }

    @Test
    void startShouldPreserveExistingStartTime() {
        ProductionTask task = task(2L);
        LocalDateTime original = LocalDateTime.of(2026, 7, 18, 8, 30);
        task.setStartTime(original);
        when(productionTaskMapper.getById(2L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(1);

        productionTaskService.start(2L);

        assertThat(task.getStartTime()).isEqualTo(original);
    }

    @Test
    void pauseShouldSetPausedStatus() {
        ProductionTask task = task(3L);
        task.setStatus(1);
        when(productionTaskMapper.getById(3L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(1);

        productionTaskService.pause(3L);

        assertThat(task.getStatus()).isEqualTo(2);
    }

    @Test
    void finishShouldFillMissingQuantitiesAndFinishTime() {
        ProductionTask task = task(4L);
        task.setPlannedQuantity(25);
        task.setCompletedQuantity(25);
        when(productionTaskMapper.getById(4L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(1);

        productionTaskService.finish(4L);

        assertThat(task.getStatus()).isEqualTo(3);
        assertThat(task.getCompletedQuantity()).isEqualTo(25);
        assertThat(task.getDefectQuantity()).isZero();
        assertThat(task.getFinishTime()).isNotNull();
    }

    @Test
    void actionShouldRejectMissingTaskWithoutUpdating() {
        when(productionTaskMapper.getById(99L)).thenReturn(null);

        assertThatThrownBy(() -> productionTaskService.start(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生产任务不存在");
        verify(productionTaskMapper, never()).update(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void actionShouldReportUpdateFailure() {
        ProductionTask task = task(5L);
        when(productionTaskMapper.getById(5L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(0);

        assertThatThrownBy(() -> productionTaskService.pause(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生产任务状态更新失败");
    }

    @Test
    void finishShouldRejectWhenCompletedBelowPlanned() {
        ProductionTask task = task(6L);
        task.setPlannedQuantity(25);
        task.setCompletedQuantity(10);
        when(productionTaskMapper.getById(6L)).thenReturn(task);

        assertThatThrownBy(() -> productionTaskService.finish(6L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未达到计划数");
        verify(productionTaskMapper, never()).update(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void finishShouldSetStatus4WhenQualityGateEnabled() {
        ProductionTask task = task(7L);
        task.setPlannedQuantity(25);
        task.setCompletedQuantity(25);
        task.setProcessRouteId(100L);
        when(productionTaskMapper.getById(7L)).thenReturn(task);
        when(processRouteMapper.getById(100L)).thenReturn(routeWithQualityGate());
        when(productionTaskMapper.update(task)).thenReturn(1);

        productionTaskService.finish(7L);

        assertThat(task.getStatus()).isEqualTo(4);
        assertThat(task.getFinishTime()).isNotNull();
    }

    @Test
    void finishShouldSetStatus3WhenNoProcessRoute() {
        ProductionTask task = task(8L);
        task.setPlannedQuantity(25);
        task.setCompletedQuantity(25);
        when(productionTaskMapper.getById(8L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(1);

        productionTaskService.finish(8L);

        assertThat(task.getStatus()).isEqualTo(3);
    }

    @Test
    void finishShouldDefaultMissingQuantitiesToZero() {
        ProductionTask task = task(9L);
        when(productionTaskMapper.getById(9L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(1);

        productionTaskService.finish(9L);

        assertThat(task.getCompletedQuantity()).isZero();
        assertThat(task.getDefectQuantity()).isZero();
        assertThat(task.getStatus()).isEqualTo(3);
    }

    @Test
    void finishShouldNotWaitForInspectionWhenRouteIsMissingOrNotQualityGate() {
        ProductionTask missingRouteTask = task(10L);
        missingRouteTask.setProcessRouteId(100L);
        missingRouteTask.setPlannedQuantity(1);
        missingRouteTask.setCompletedQuantity(1);
        ProductionTask ordinaryTask = task(11L);
        ordinaryTask.setProcessRouteId(101L);
        ordinaryTask.setPlannedQuantity(1);
        ordinaryTask.setCompletedQuantity(1);
        ProcessRoute ordinaryRoute = new ProcessRoute();
        ordinaryRoute.setQualityGate(0);
        when(productionTaskMapper.getById(10L)).thenReturn(missingRouteTask);
        when(productionTaskMapper.getById(11L)).thenReturn(ordinaryTask);
        when(processRouteMapper.getById(100L)).thenReturn(null);
        when(processRouteMapper.getById(101L)).thenReturn(ordinaryRoute);
        when(productionTaskMapper.update(missingRouteTask)).thenReturn(1);
        when(productionTaskMapper.update(ordinaryTask)).thenReturn(1);

        productionTaskService.finish(10L);
        productionTaskService.finish(11L);

        assertThat(missingRouteTask.getStatus()).isEqualTo(3);
        assertThat(ordinaryTask.getStatus()).isEqualTo(3);
    }

    @Test
    void finishShouldRejectMissingTask() {
        assertThatThrownBy(() -> productionTaskService.finish(404L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生产任务不存在");
        verify(productionTaskMapper, never()).update(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void finishShouldReportUpdateFailure() {
        ProductionTask task = task(12L);
        task.setPlannedQuantity(1);
        task.setCompletedQuantity(1);
        when(productionTaskMapper.getById(12L)).thenReturn(task);
        when(productionTaskMapper.update(task)).thenReturn(0);

        assertThatThrownBy(() -> productionTaskService.finish(12L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生产任务状态更新失败");
    }

    private ProductionTask task(Long id) {
        ProductionTask task = new ProductionTask();
        task.setId(id);
        return task;
    }

    private ProcessRoute routeWithQualityGate() {
        ProcessRoute route = new ProcessRoute();
        route.setQualityGate(1);
        return route;
    }
}

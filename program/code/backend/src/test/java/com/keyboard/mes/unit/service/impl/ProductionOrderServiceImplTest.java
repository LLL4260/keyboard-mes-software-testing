package com.keyboard.mes.unit.service.impl;

import com.keyboard.mes.entity.ProcessRoute;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.exception.BusinessException;
import com.keyboard.mes.repository.ProcessRouteMapper;
import com.keyboard.mes.repository.ProductionOrderMapper;
import com.keyboard.mes.repository.ProductionTaskMapper;
import com.keyboard.mes.service.impl.ProductionOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ProductionOrderServiceImplTest {

    @Mock
    private ProductionOrderMapper productionOrderMapper;

    @Mock
    private ProcessRouteMapper processRouteMapper;

    @Mock
    private ProductionTaskMapper productionTaskMapper;

    @InjectMocks
    private ProductionOrderServiceImpl productionOrderService;

    @Test
    void basicOperationsShouldReturnMapperDataAndReportWriteOutcomes() {
        ProductionOrder order = order();
        ArrayList<ProductionOrder> orders = new ArrayList<>(List.of(order));
        when(productionOrderMapper.productionOrderList()).thenReturn(orders);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(productionOrderMapper.insert(order)).thenReturn(1, 0);
        when(productionOrderMapper.delete(1L)).thenReturn(1, 0);
        when(productionOrderMapper.update(order)).thenReturn(1, 0);

        assertThat(productionOrderService.productionOrderList()).isSameAs(orders);
        assertThat(productionOrderService.getProductionOrderById(1L)).isSameAs(order);
        assertThat(productionOrderService.save(order)).isTrue();
        assertThat(productionOrderService.save(order)).isFalse();
        assertThat(productionOrderService.delete(1L)).isTrue();
        assertThat(productionOrderService.delete(1L)).isFalse();
        assertThat(productionOrderService.update(order)).isTrue();
        assertThat(productionOrderService.update(order)).isFalse();
    }

    @Test
    void generateTasksShouldCreateTasksFromActiveRoutes() {
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        order.setOrderNo("MO-001");
        order.setProductModelId(10L);
        order.setRouteVersion("R1.0");
        order.setQuantity(50);

        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>());
        when(processRouteMapper.processRouteList()).thenReturn(new ArrayList<>(List.of(
                route(11L, 10L, "R1.0", 20, "ST-02", 1),
                route(12L, 10L, "R1.0", 10, "ST-01", 1),
                route(13L, 10L, "R1.0", 30, "ST-03", 0),
                route(14L, 99L, "R1.0", 40, "ST-04", 1)
        )));
        when(productionTaskMapper.insert(any(ProductionTask.class))).thenReturn(1);
        when(productionOrderMapper.update(order)).thenReturn(1);

        ArrayList<ProductionTask> tasks = productionOrderService.generateTasks(1L);

        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(ProductionTask::getSequenceNo).containsExactly(10, 20);
        assertThat(tasks.get(0).getTaskNo()).isEqualTo("TASK-MO-001-010");
        assertThat(tasks.get(0).getPlannedQuantity()).isEqualTo(50);
        assertThat(tasks.get(0).getCompletedQuantity()).isZero();
        assertThat(tasks.get(0).getDefectQuantity()).isZero();
        assertThat(tasks.get(0).getStatus()).isZero();
        assertThat(order.getStatus()).isEqualTo(1);

        ArgumentCaptor<ProductionTask> taskCaptor = ArgumentCaptor.forClass(ProductionTask.class);
        verify(productionTaskMapper, times(2)).insert(taskCaptor.capture());
        assertThat(taskCaptor.getAllValues()).hasSize(2);
        verify(productionOrderMapper).update(order);
    }

    @Test
    void generateTasksShouldReturnExistingTasksWithoutDuplicating() {
        ProductionOrder order = order();
        ProductionTask existing = new ProductionTask();
        existing.setId(30L);
        existing.setOrderId(1L);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>(List.of(existing)));

        ArrayList<ProductionTask> tasks = productionOrderService.generateTasks(1L);

        assertThat(tasks).containsExactly(existing);
        verify(processRouteMapper, never()).processRouteList();
        verify(productionTaskMapper, never()).insert(any());
    }

    @Test
    void generateTasksShouldRejectMissingOrder() {
        when(productionOrderMapper.getById(404L)).thenReturn(null);

        assertThatThrownBy(() -> productionOrderService.generateTasks(404L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生产工单不存在");
        verifyNoTaskMutation();
    }

    @Test
    void generateTasksShouldRejectWhenNoEnabledMatchingRouteExists() {
        ProductionOrder order = order();
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>());
        when(processRouteMapper.processRouteList()).thenReturn(new ArrayList<>(List.of(
                route(11L, 10L, "R2.0", 10, "ST-01", 1),
                route(12L, 10L, "R1.0", 20, "ST-02", 0)
        )));

        assertThatThrownBy(() -> productionOrderService.generateTasks(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("未找到匹配的启用工艺路线");
        verify(productionTaskMapper, never()).insert(any());
    }

    @Test
    void generateTasksShouldStopWhenTaskInsertFails() {
        ProductionOrder order = order();
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>());
        when(processRouteMapper.processRouteList()).thenReturn(new ArrayList<>(List.of(
                route(11L, 10L, "R1.0", 10, "ST-01", 1)
        )));
        when(productionTaskMapper.insert(any())).thenReturn(0);

        assertThatThrownBy(() -> productionOrderService.generateTasks(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生成生产任务失败");
        verify(productionOrderMapper, never()).update(order);
    }

    @Test
    void generateTasksShouldAcceptUnspecifiedRouteStatusAndUseFallbackTaskNumber() {
        ProductionOrder order = order();
        order.setOrderNo(" ");
        ProcessRoute route = route(11L, 10L, "R1.0", null, "ST-01", null);
        when(productionOrderMapper.getById(1L)).thenReturn(order);
        when(productionTaskMapper.productionTaskList()).thenReturn(new ArrayList<>());
        when(processRouteMapper.processRouteList()).thenReturn(new ArrayList<>(List.of(route)));
        when(productionTaskMapper.insert(any())).thenReturn(1);

        ArrayList<ProductionTask> tasks = productionOrderService.generateTasks(1L);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTaskNo()).isEqualTo("TASK-1-000");
        assertThat(tasks.get(0).getSequenceNo()).isNull();
        assertThat(order.getStatus()).isEqualTo(1);
    }

    private ProductionOrder order() {
        ProductionOrder order = new ProductionOrder();
        order.setId(1L);
        order.setOrderNo("MO-001");
        order.setProductModelId(10L);
        order.setRouteVersion("R1.0");
        order.setQuantity(50);
        return order;
    }

    private void verifyNoTaskMutation() {
        verify(productionTaskMapper, never()).productionTaskList();
        verify(productionTaskMapper, never()).insert(any());
    }

    private ProcessRoute route(Long id, Long productModelId, String routeVersion, Integer sequenceNo,
                               String stationCode, Integer status) {
        ProcessRoute route = new ProcessRoute();
        route.setId(id);
        route.setProductModelId(productModelId);
        route.setRouteVersion(routeVersion);
        route.setSequenceNo(sequenceNo);
        route.setStationCode(stationCode);
        route.setStatus(status);
        return route;
    }
}

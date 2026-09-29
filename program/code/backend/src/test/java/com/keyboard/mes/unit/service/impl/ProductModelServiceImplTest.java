package com.keyboard.mes.unit.service.impl;

import com.keyboard.mes.entity.ProductModel;
import com.keyboard.mes.repository.ProductModelMapper;
import com.keyboard.mes.service.impl.ProductModelServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ProductModelServiceImplTest {

    @Mock
    private ProductModelMapper productModelMapper;
    @InjectMocks
    private ProductModelServiceImpl productModelService;

    @Test
    void listShouldReturnMapperRecords() {
        ProductModel model = model();
        when(productModelMapper.productModelList()).thenReturn(new ArrayList<>(List.of(model)));
        assertThat(productModelService.productModelList()).containsExactly(model);
    }

    @Test
    void getShouldForwardId() {
        ProductModel model = model();
        when(productModelMapper.getById(1L)).thenReturn(model);
        assertThat(productModelService.getProductModelById(1L)).isSameAs(model);
        verify(productModelMapper).getById(1L);
    }

    @Test
    void saveShouldReflectInsertResult() {
        ProductModel model = model();
        when(productModelMapper.insert(model)).thenReturn(1, 0);
        assertThat(productModelService.save(model)).isTrue();
        assertThat(productModelService.save(model)).isFalse();
    }

    @Test
    void updateShouldReflectMapperResult() {
        ProductModel model = model();
        when(productModelMapper.update(model)).thenReturn(1, 0);
        assertThat(productModelService.update(model)).isTrue();
        assertThat(productModelService.update(model)).isFalse();
    }

    @Test
    void deleteShouldReflectMapperResult() {
        when(productModelMapper.delete(1L)).thenReturn(1, 0);
        assertThat(productModelService.delete(1L)).isTrue();
        assertThat(productModelService.delete(1L)).isFalse();
    }

    private ProductModel model() {
        ProductModel model = new ProductModel();
        model.setId(1L);
        model.setModelCode("KB-001");
        return model;
    }
}

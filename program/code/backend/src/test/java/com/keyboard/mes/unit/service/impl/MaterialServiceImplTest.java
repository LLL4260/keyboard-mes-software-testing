package com.keyboard.mes.unit.service.impl;

import com.keyboard.mes.entity.Material;
import com.keyboard.mes.repository.MaterialMapper;
import com.keyboard.mes.service.impl.MaterialServiceImpl;
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
class MaterialServiceImplTest {

    @Mock
    private MaterialMapper materialMapper;
    @InjectMocks
    private MaterialServiceImpl materialService;

    @Test
    void listShouldReturnMapperRecords() {
        Material material = material();
        when(materialMapper.materialList()).thenReturn(new ArrayList<>(List.of(material)));
        assertThat(materialService.materialList()).containsExactly(material);
    }

    @Test
    void getShouldForwardId() {
        Material material = material();
        when(materialMapper.getById(1L)).thenReturn(material);
        assertThat(materialService.getMaterialById(1L)).isSameAs(material);
        verify(materialMapper).getById(1L);
    }

    @Test
    void saveShouldReflectInsertResult() {
        Material material = material();
        when(materialMapper.insert(material)).thenReturn(1, 0);
        assertThat(materialService.save(material)).isTrue();
        assertThat(materialService.save(material)).isFalse();
    }

    @Test
    void updateShouldReflectMapperResult() {
        Material material = material();
        when(materialMapper.update(material)).thenReturn(1, 0);
        assertThat(materialService.update(material)).isTrue();
        assertThat(materialService.update(material)).isFalse();
    }

    @Test
    void deleteShouldReflectMapperResult() {
        when(materialMapper.delete(1L)).thenReturn(1, 0);
        assertThat(materialService.delete(1L)).isTrue();
        assertThat(materialService.delete(1L)).isFalse();
    }

    private Material material() {
        Material material = new Material();
        material.setId(1L);
        material.setMaterialCode("MAT-001");
        return material;
    }
}

package com.example.consumer.pojo.vo;

import com.example.consumer.pojo.dto.DormitoryFindByUniversityUuidDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DormitoryBuildingVO {
    private List<DormitoryFindByUniversityUuidDTO> dormitoryBuildings;
}

package com.example.consumer.pojo.vo;

import com.example.consumer.pojo.dto.FloorDTO;
import com.example.consumer.pojo.dto.TreeFloorDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DormitoryFloorVO {
    private List<TreeFloorDTO> roomLocation;
}

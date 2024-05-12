package com.example.consumer.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DormitoryFindByUniversityUuidDTO {

    /**
     * 几号楼
     */
    private String title;

    /**
     * uuid
     */
    private String value;

    /**
     * id
     */
    private String buildingId;

    /**
     * 关联子集
     */
    private List<DormitoryFindByUniversityUuidDTO> children;

    /**
     * 回显字符
     */

    private String titleChain;

}

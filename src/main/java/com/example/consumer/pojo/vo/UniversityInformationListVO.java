package com.example.consumer.pojo.vo;

import com.example.consumer.pojo.dto.UniversityInformationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityInformationListVO implements Serializable {
    private List<UniversityInformationDTO> universityInformationDTOList;

}

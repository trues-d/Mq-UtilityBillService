package com.example.consumer.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityInformationDTO  implements Serializable {
    private  String title;
    private  String value;
    private List<UniversityInformationDTO> children;

    private String titleChain;
}

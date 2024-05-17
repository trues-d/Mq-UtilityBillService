package com.example.consumer.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeFloorDTO implements Comparable{
    private String title;
    private String value ;
    private String floorRoom;
    private List<TreeFloorDTO> children;

    @Override
    public int compareTo(Object o) {
        TreeFloorDTO tempObject = (TreeFloorDTO) o;
        return Integer.parseInt(this.getFloorRoom()) < Integer.parseInt(tempObject.getFloorRoom())? -1 :1;
    }
}

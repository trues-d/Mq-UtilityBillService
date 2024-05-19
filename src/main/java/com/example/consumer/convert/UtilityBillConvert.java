package com.example.consumer.convert;

import com.example.consumer.pojo.dto.*;
import com.example.consumer.pojo.po.DormitoryAreaPO;
import com.example.consumer.pojo.po.UniversityCodePO;
import com.example.consumer.pojo.po.UtilityBillUserDTOPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;


// 表示生成的 Mapper 实现类会被 Spring 框架管理，可以通过 Spring 的依赖注入机制进行注入和使用。
@Mapper(componentModel = "spring")
public interface UtilityBillConvert {


    @Mappings({
            @Mapping(source = "universityRegion",target ="title"),
            @Mapping(source = "codeId",target ="value"),
            @Mapping(target = "children",expression = "java(new java.util.ArrayList<UniversityInformationDTO>())"),
            @Mapping(target = "titleChain",expression = "java(new String())")
    })
    UniversityInformationDTO universityCodePOToUniversityInformationDTO(UniversityCodePO universityCodePO);

    @Mappings({
            @Mapping(source = "name",target = "title"),
            @Mapping(source = "uuid",target = "value"),
            @Mapping(target = "buildingId",expression ="java(new String())" ),
            @Mapping(source = "name",target = "titleChain" ),
            @Mapping(target = "children",expression = "java(new java.util.ArrayList<>())")
    })
    DormitoryFindByUniversityUuidDTO dormitoryAreaPOToDormitoryFindByUniversityUuidDTO(DormitoryAreaPO dormitoryAreaPO);


    @Mappings({
            @Mapping(source = "floorRoomShow",target = "title"),
            @Mapping(source = "floorRoom",target = "value"),
            @Mapping(source = "floorRoomShow",target = "floorRoom"),
            @Mapping(target = "children",expression = "java(new java.util.ArrayList<>())")
    })
    TreeFloorDTO floorDTOToTreeFloorDTO(FloorDTO floorDTO);

    @Mappings({
            @Mapping(source ="email" ,target = "mail"),
            @Mapping(source ="userName" ,target = "userName"),
            @Mapping(source ="universityCodeId" ,target = "universityCodeId"),
            @Mapping(target ="dormitoryId" ,expression = "java(0)"),
            @Mapping(target ="dormitoryRoomId" ,expression = "java(0)"),
            @Mapping(target = "ifDeleted",expression= "java(0)")
    })
    UtilityBillUserDTOPO userSignUpDTOToUtilityBillUserDTOPO(UserSignUpDTO userSignUpDTO);

}

package com.example.consumer.service;

import com.example.consumer.pojo.dto.UserSignUpDTO;
import com.example.consumer.pojo.vo.DormitoryBuildingVO;
import com.example.consumer.pojo.vo.DormitoryFloorVO;
import com.example.consumer.pojo.vo.UniversityInformationListVO;

public interface IUserLoginService {
     UniversityInformationListVO getUniversityInformation();

     void userSignUpVerify(UserSignUpDTO userSignUpDTO);

     DormitoryBuildingVO getDormitoryDetails(String universityUuid);
     DormitoryFloorVO getDormitoryRoom();

}

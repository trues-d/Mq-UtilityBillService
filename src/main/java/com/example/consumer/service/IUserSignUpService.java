package com.example.consumer.service;

import com.example.consumer.pojo.dto.UserSignUpDTO;
import com.example.consumer.pojo.dto.UserSignUpRespDTO;
import com.example.consumer.pojo.vo.DormitoryBuildingVO;
import com.example.consumer.pojo.vo.DormitoryFloorVO;
import com.example.consumer.pojo.vo.UniversityInformationListVO;

import javax.servlet.http.HttpServletResponse;

public interface IUserSignUpService {
     UniversityInformationListVO getUniversityInformation();

     UserSignUpRespDTO userSignUpVerify(UserSignUpDTO userSignUpDTO);

     DormitoryBuildingVO getDormitoryDetails(String universityUuid);
     DormitoryFloorVO getDormitoryRoom();
     void verifyUserUuid(String userUuid, HttpServletResponse response);

}

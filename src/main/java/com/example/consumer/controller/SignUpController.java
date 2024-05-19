package com.example.consumer.controller;

import com.example.consumer.pojo.dto.UserSignUpDTO;
import com.example.consumer.pojo.dto.UserSignUpRespDTO;
import com.example.consumer.pojo.vo.DormitoryBuildingVO;
import com.example.consumer.pojo.vo.DormitoryFloorVO;
import com.example.consumer.pojo.vo.UniversityInformationListVO;
import com.example.consumer.service.IUserSignUpService;
import com.example.consumer.utils.WebResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/utilityBill/signUp")
@RequiredArgsConstructor
@CrossOrigin
public class SignUpController {

    private final IUserSignUpService userSignUpService;

    /**
     *  查询学校信息数据用于返回学校信息列表 给前端做下拉列表展示
     *
     * @return [] 返回的学校信息列表
     */
    @GetMapping("/dropDownList/getUniversityAndArea")
    public WebResponseUtil<UniversityInformationListVO> getUniversityAndArea(){
        return    WebResponseUtil.Success(userSignUpService.getUniversityInformation());
    }

    /**
     * 根据选择的学校和校区 回显宿舍相关信息
     *
     * @param universityUuid: 学校-校区的唯一uuid
     * @return com.example.consumer.utils.WebResponseUtil<com.example.consumer.pojo.vo.DormitoryBuildingVO>
     */

    @GetMapping("/dropDownList/getDormitoryDetails")
    public WebResponseUtil<DormitoryBuildingVO> getDormitoryDetails(@RequestParam String universityUuid){
        return WebResponseUtil.Success(userSignUpService.getDormitoryDetails(universityUuid));
    }

    @GetMapping("/dropDownList/getDormitoryFloor")
    public WebResponseUtil<DormitoryFloorVO> getDormitoryFloor(){
        return WebResponseUtil.Success(userSignUpService.getDormitoryRoom());
    }

    @PostMapping("/userSignUp")
    public WebResponseUtil<UserSignUpRespDTO> signUp(@Validated  @RequestBody UserSignUpDTO userSignUpDTO){
        System.out.println(userSignUpDTO.toString());
        UserSignUpRespDTO userSignUpRespDTO = userSignUpService.userSignUpVerify(userSignUpDTO);
        return WebResponseUtil.Success(userSignUpRespDTO);
    }

    @GetMapping("/userSignUp/verify")
    public WebResponseUtil<Void> signUpVerify(@RequestParam(name = "signUpUUID") String uuid, HttpServletResponse response){
        System.out.println(uuid);
        userSignUpService.verifyUserUuid(uuid,response);
        return WebResponseUtil.Success();
    }


}

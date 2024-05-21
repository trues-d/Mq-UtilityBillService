package com.example.consumer.controller;

import com.example.consumer.pojo.dto.UserSignUpDTO;
import com.example.consumer.pojo.dto.UserSignUpRespDTO;
import com.example.consumer.pojo.vo.DormitoryBuildingVO;
import com.example.consumer.pojo.vo.DormitoryFloorVO;
import com.example.consumer.pojo.vo.UniversityInformationListVO;
import com.example.consumer.service.IUserSignUpService;
import com.example.consumer.utils.WebResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/utilityBill/signUp")
@RequiredArgsConstructor
@Slf4j
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

    /**
     * 获取宿舍楼层信息
     *
     * @param : 
     * @return com.example.consumer.utils.WebResponseUtil<com.example.consumer.pojo.vo.DormitoryFloorVO>
     */
    
    @GetMapping("/dropDownList/getDormitoryFloor")
    public WebResponseUtil<DormitoryFloorVO> getDormitoryFloor(){
        return WebResponseUtil.Success(userSignUpService.getDormitoryRoom());
    }

    /**
     * 用户注册表单  完成注册功能
     *
     * @param userSignUpDTO: 用户注册表单
     * @return com.example.consumer.utils.WebResponseUtil<com.example.consumer.pojo.dto.UserSignUpRespDTO>
     */
    
    @PostMapping("/userSignUp")
    public WebResponseUtil<UserSignUpRespDTO> signUp(@Validated  @RequestBody UserSignUpDTO userSignUpDTO){
        log.info(userSignUpDTO.toString());
        UserSignUpRespDTO userSignUpRespDTO = userSignUpService.userSignUpVerify(userSignUpDTO);
        return WebResponseUtil.Success(userSignUpRespDTO);
    }

    /**
     * 点击邮件链接完成登录
     *
     * @param uuid: 邮件链接中uuid
     * @param response: HttpServletResponse
     * @return com.example.consumer.utils.WebResponseUtil<java.lang.Void>
     */

    @GetMapping("/userSignUp/verify")
    public WebResponseUtil<Void> signUpVerify(@RequestParam(name = "signUpUUID") String uuid, HttpServletResponse response){
        log.info(String.format("signUpUuid  ==> %s",uuid));
        userSignUpService.verifyUserUuid(uuid,response);
        return WebResponseUtil.Success();
    }


}

package com.example.consumer.controller;


import com.example.consumer.pojo.dto.UserSignUpDTO;
import com.example.consumer.pojo.vo.DormitoryBuildingVO;
import com.example.consumer.pojo.vo.UniversityInformationListVO;
import com.example.consumer.service.impl.UserSignUpService;
import com.example.consumer.service.impl.UtilityBillsService;
import com.example.consumer.utils.WebResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/utilityBill")
@RequiredArgsConstructor
@CrossOrigin
public class UtilityBillController {
    private final UtilityBillsService utilityBillsService;
    private final UserSignUpService userSignUpService;

    /**
     * 针对已经注册的用户，传入用户邮箱，即可查询用户宿舍的水电费情况
     * 并发送邮件信息给用户
     *
     * @param recipient: 用户接收方的邮件
     * @return [java.lang.String]
     */

    @GetMapping("/getBill")
    public WebResponseUtil<Void> getUtilityBill(@RequestParam(required = false ,value = "mail") String recipient){
        utilityBillsService.sendBill(recipient);
        return WebResponseUtil.Success();
    }

    /**
     *  查询学校信息数据用于返回学校信息列表 给前端做下拉列表展示
     *
     * @return [] 返回的学校信息列表
     */

    @GetMapping("/getUniversityAndArea")
    public WebResponseUtil<UniversityInformationListVO> getUniversityAndArea(){
        return    WebResponseUtil.Success(userSignUpService.getUniversityInformation());
    }

    @GetMapping("/getDormitoryDetails")
        public WebResponseUtil<DormitoryBuildingVO> getDormitoryDetails(@RequestParam String universityUuid){

        return WebResponseUtil.Success(userSignUpService.getDormitoryDetails(universityUuid));

    }


    @PostMapping("/userSignUp")
    public WebResponseUtil<Void> SignUp(@Validated  @RequestBody UserSignUpDTO userSignUpDTO){
        System.out.println(userSignUpDTO.toString());
        userSignUpService.userSignUpVerify(userSignUpDTO);
        return WebResponseUtil.Success();

    }

    @GetMapping("/userSignUp")
    public WebResponseUtil<Void> SignUpVerify(@RequestParam(name = "signUpUUID") String uuid){
        System.out.println(uuid);
        return WebResponseUtil.Success();
    }

}

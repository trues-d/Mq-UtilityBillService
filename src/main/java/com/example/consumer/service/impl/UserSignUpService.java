package com.example.consumer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.consumer.convert.UtilityBillConvert;
import com.example.consumer.dao.DormitoryCodeDao;
import com.example.consumer.mapper.UniversityCodeMapper;
import com.example.consumer.mapper.UtilityBillUserMapper;
import com.example.consumer.pojo.dto.DormitoryFindByUniversityUuidDTO;
import com.example.consumer.pojo.dto.UniversityInformationDTO;
import com.example.consumer.pojo.dto.UserSignUpDTO;
import com.example.consumer.pojo.po.DormitoryAreaPO;
import com.example.consumer.pojo.po.DormitoryCodePO;
import com.example.consumer.pojo.po.UniversityCodePO;
import com.example.consumer.pojo.vo.DormitoryBuildingVO;
import com.example.consumer.pojo.vo.UniversityInformationListVO;
import com.example.consumer.service.IUserLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSignUpService implements IUserLoginService {
    private final UniversityCodeMapper universityCodeMapper;
    private final UtilityBillUserMapper utilityBillUserMapper;
    private final MailSendingService mailSendingService;
    private final UtilityBillConvert utilityBillConvert;

    @Resource
    private DormitoryCodeDao dormitoryCodeDao;

    private static final Integer UUID_SUBSTRING_BEGIN = 0;
    private static final Integer UUID_SUBSTRING_NED = 16;


    @Override
    public UniversityInformationListVO getUniversityInformation() {
        List<UniversityCodePO> universityAndArea = universityCodeMapper.selectList(null);

        HashMap<String, UniversityInformationDTO> resultMap = new HashMap<>();
        universityAndArea.forEach(universityCodePO -> {
            // convert转换成UniversityInformationDTO对象 含有子集链表
            UniversityInformationDTO universityInformationDTO = utilityBillConvert.universityCodePOToUniversityInformationDTO(universityCodePO);
            String chain = String.format("-%s", universityCodePO.getUniversityRegion());
            // 如果结果集的key中含有上述对象的学校名，就代表是学校的多个校区，一个校区也是校区依然以tree的子树节点表示
            // 如果校区名字为null 那么就没有子树
            if (resultMap.containsKey(universityCodePO.getUniversityName())) {

                UniversityInformationDTO universityFather = resultMap.get(universityCodePO.getUniversityName());
                universityInformationDTO.setTitleChain(universityFather.getTitleChain().concat(chain));
                universityFather.getChildren().add(universityInformationDTO);
                universityFather.setValue(IdWorker.get32UUID().substring(UUID_SUBSTRING_BEGIN, UUID_SUBSTRING_NED));  //生成一个16位的随机uuid 无意义 只是为了前端的唯一显示
            } else {
                // key不在map中就加入 title改成学校名字 children加入校区的新UniversityInformationDTO对象
                universityInformationDTO.setTitle(universityCodePO.getUniversityName());
                universityInformationDTO.setTitleChain(universityCodePO.getUniversityName());
                if (!universityCodePO.getUniversityRegion().isEmpty()) {
                    // 非空表示有校区名字
                    UniversityInformationDTO childrenItem = utilityBillConvert.universityCodePOToUniversityInformationDTO(universityCodePO);
                    childrenItem.setTitleChain(universityCodePO.getUniversityName().concat(chain));
                    universityInformationDTO.getChildren().add(childrenItem);
                }
                resultMap.put(universityCodePO.getUniversityName(), universityInformationDTO);
            }
        });
        //转换成VO对象返回
        return new UniversityInformationListVO(new ArrayList<>(resultMap.values()));
    }

    @Override
    public void userSignUpVerify(UserSignUpDTO userSignUpDTO) {
        // Collections.singletonList(userSignUpDTO.getMail()) 创建包含字符串的不可改变的列表
        if (utilityBillUserMapper.selectBatchIds(Collections.singletonList(userSignUpDTO.getMail())).size()==0) {
            mailSendingService.sendHtmlMailFormQQMail(userSignUpDTO.getMail(), userSignUpDTO.getUserName(), UUID.randomUUID().toString());
        }
    }


    /**
     * 根据校区uuid 返回学校的住宿信息 住宿区-住宿楼-住宿楼代表编号
     *
     * @param universityUuid: 学校-校区唯一uuid
     * @return void
     */

    @Override
    public DormitoryBuildingVO getDormitoryDetails(String universityUuid) {
        if(Objects.isNull(universityUuid) || universityUuid.isEmpty()){
            return new DormitoryBuildingVO(new ArrayList<>());
        }

        List<DormitoryCodePO> dormitoryDetails = dormitoryCodeDao.selectListByUniversityCode(universityUuid);
        // key是住宿区唯一id value是住宿区的名字
        Map<String, DormitoryAreaPO> specificDormitoryArea = dormitoryCodeDao.selectMapDormitoryUuidToName(universityUuid);
        Map<String, DormitoryFindByUniversityUuidDTO> collect = specificDormitoryArea.values()
                .stream()
                .map(utilityBillConvert::dormitoryAreaPOToDormitoryFindByUniversityUuidDTO)
                .collect(Collectors.toMap(DormitoryFindByUniversityUuidDTO::getValue, i -> i));

        dormitoryDetails.forEach(dormitoryItem -> {
            // 获取住宿区名字
            DormitoryAreaPO dormitoryAreaItem = specificDormitoryArea.get(dormitoryItem.getDormitoryAreaUuid());
            String dormitoryAreaName = dormitoryAreaItem.getName();
            if (Objects.nonNull(dormitoryAreaName)) {
                int buildingIndex = dormitoryItem.getName().indexOf(dormitoryAreaName) + dormitoryAreaName.length();
                String buildingName = dormitoryItem.getName().substring(buildingIndex);
                // 将宿舍楼信息转换成DormitoryFindByUniversityUuidDTO对象
                DormitoryFindByUniversityUuidDTO returnItem =
                        new DormitoryFindByUniversityUuidDTO(buildingName,
                                dormitoryItem.getUuid(),
                                dormitoryItem.getCodeId(),
                                new ArrayList<>(),
                                collect.get(dormitoryItem.getDormitoryAreaUuid())
                                        .getTitle()
                                        .concat(String.format("-%s",buildingName)));
                // 存入Map
                collect.get(dormitoryItem.getDormitoryAreaUuid()).getChildren().add(returnItem);
            }
        });
        return new DormitoryBuildingVO(new ArrayList<>(collect.values()));


    }
}

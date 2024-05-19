package com.example.consumer.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.consumer.convert.UtilityBillConvert;
import com.example.consumer.dao.DormitoryCodeDao;
import com.example.consumer.mapper.UniversityCodeMapper;
import com.example.consumer.mapper.UtilityBillUserMapper;
import com.example.consumer.pojo.dto.*;
import com.example.consumer.pojo.entity.UserSignUpEnum;
import com.example.consumer.pojo.po.DormitoryAreaPO;
import com.example.consumer.pojo.po.DormitoryCodePO;
import com.example.consumer.pojo.po.UniversityCodePO;
import com.example.consumer.pojo.po.UtilityBillUserDTOPO;
import com.example.consumer.pojo.vo.DormitoryBuildingVO;
import com.example.consumer.pojo.vo.DormitoryFloorVO;
import com.example.consumer.pojo.vo.UniversityInformationListVO;
import com.example.consumer.service.IUserSignUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j

public class UserSignUpService implements IUserSignUpService {
    @Resource
    private  UniversityCodeMapper universityCodeMapper;
    @Resource
    private  UtilityBillUserMapper utilityBillUserMapper;
    @Resource
    private  MailSendingService mailSendingService;
    @Resource
    private  UtilityBillConvert utilityBillConvert;

    @Resource
    private  JedisPool jedisPool;

    @Resource
    private DormitoryCodeDao dormitoryCodeDao;

    @Resource
    private UtilityBillUserService utilityBillUserService;


    private static final Integer UUID_SUBSTRING_BEGIN = 0;
    private static final Integer UUID_SUBSTRING_NED = 16;
    /**
     * 每个宿舍楼最少1层
     */
    private static final Integer MIN_FLOOR_NUM = 1;
    /**
     * 每个宿舍楼，最多20层
     */
    private static final Integer MAX_FLOOR_NUM = 20;

    /**
     * 宿舍楼每层最少1个房间
     */
    private static final Integer MIN_FLOOR_ROOM_NUM = 1;

    /**
     * 宿舍楼每层最多30个房间
     */
    private static final Integer MAX_FLOOR_ROOM_NUM = 30;

    /**
     * 用户注册生成的uuid的过期时间
     */

    private static final Duration Minutes_5 = Duration.ofMinutes(5L);

    /**
     * 用户注册时的空间
     */
    private static final String User_SignUp_Key="user:signUp:%s";



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


    /**
     * 邮箱验证登录
     *
     * @param userSignUpDTO:
     */

    @Override
    public UserSignUpRespDTO userSignUpVerify(UserSignUpDTO userSignUpDTO) {
        UserSignUpRespDTO userSignUpRespDTO = new UserSignUpRespDTO();
        // 如果还没有注册就发送邮件
        if (utilityBillUserMapper.selectBatchIds(Collections.singletonList(userSignUpDTO.getEmail())).size()==0) {
            // 16位的uuid
            UUID uuid = UUID.randomUUID();
            String fullUUID = uuid.toString().replace("-", "");
            String userUuid = fullUUID.substring(0, 16);
            try{
                Jedis resource = jedisPool.getResource();
                long exSeconds = Minutes_5.getSeconds();
                resource.setex(String.format(User_SignUp_Key, userUuid),exSeconds,JSON.toJSONString(userSignUpDTO));
                resource.close();
                mailSendingService.sendHtmlMailFormQQMail(userSignUpDTO.getEmail(), userSignUpDTO.getUserName(), userUuid);
            }catch (Exception exception){
                log.error("邮件发送失败",exception);
            }
            userSignUpRespDTO.setVerifyCode(UserSignUpEnum.USER_NEVER_SIGNUP.getValue());
            userSignUpRespDTO.setSignUpMsg(UserSignUpEnum.USER_NEVER_SIGNUP.getSignUpMsg());
            userSignUpRespDTO.setInformMsg("登录校验邮件已经发送");
            userSignUpRespDTO.setUserUuid(userUuid);
        }else{
            // 否则表示已经注册了 表示可以直接跳转到到登录页面
            userSignUpRespDTO.setVerifyCode(UserSignUpEnum.USER_HAS_SIGNUP.getValue());
            userSignUpRespDTO.setSignUpMsg(UserSignUpEnum.USER_HAS_SIGNUP.getSignUpMsg());
            userSignUpRespDTO.setInformMsg("登录校验邮件未发送");
            userSignUpRespDTO.setUserUuid("");

        }
        return userSignUpRespDTO;
    }


    /**
     * 根据校区uuid 返回学校的住宿信息 住宿区-住宿楼-住宿楼代表编号
     *
     * @param universityUuid: 学校-校区唯一uuid
     * @return void
     */

    @Override
    public DormitoryBuildingVO getDormitoryDetails(String universityUuid) {
        if (Objects.isNull(universityUuid) || universityUuid.isEmpty()) {
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
                                String.valueOf(dormitoryItem.getCodeId()),
                                new ArrayList<>(),
                                collect.get(dormitoryItem.getDormitoryAreaUuid())
                                        .getTitle()
                                        .concat(String.format("-%s", buildingName)));
                // 存入Map
                collect.get(dormitoryItem.getDormitoryAreaUuid()).getChildren().add(returnItem);
            }
        });
        return new DormitoryBuildingVO(new ArrayList<>(collect.values()));
    }

    @Override
    public DormitoryFloorVO getDormitoryRoom() {
        HashMap<String, List<FloorDTO>> floorToRoomMap = new HashMap<>();
        for (int floorNum = MIN_FLOOR_NUM; floorNum <= MAX_FLOOR_NUM; floorNum++) {
            List<FloorDTO> floorsList = new ArrayList<>();
            String floorNumNew = floorNum < 10 ? "0".concat(String.valueOf(floorNum)):String.valueOf(floorNum);
            for (int roomNum = MIN_FLOOR_ROOM_NUM; roomNum <= MAX_FLOOR_ROOM_NUM; roomNum++) {
                FloorDTO floorDTO = new FloorDTO();
                String roomNumNew = roomNum < 10 ? "0".concat(String.valueOf(roomNum)):String.valueOf(roomNum);
                floorDTO.setFloor(floorNumNew);
                floorDTO.setRoom(roomNumNew);
                floorDTO.setFloorRoom(floorNumNew.concat(roomNumNew));
                floorDTO.setFloorRoomShow(getFloorRoom(floorNum,roomNum));

                floorsList.add(floorDTO);
            }
            floorToRoomMap.put(String.valueOf(floorNum),floorsList);
        }

        Map<String, TreeFloorDTO> result = floorToRoomMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        (item) -> {
                            List<FloorDTO> floorDTOList = item.getValue();
                            TreeFloorDTO treeFloorDTO = new TreeFloorDTO();
                            treeFloorDTO.setTitle(item.getKey().concat("楼"));
                            treeFloorDTO.setValue(item.getKey());
                            treeFloorDTO.setFloorRoom(item.getKey());
                            treeFloorDTO.setChildren(
                                    floorDTOList.stream()
                                            .map(utilityBillConvert::floorDTOToTreeFloorDTO)
                                            .collect(Collectors.toList()));
                            return treeFloorDTO;

                        }));
        DormitoryFloorVO dormitoryFloorVO = new DormitoryFloorVO(new ArrayList<>(result.values()));
        Collections.sort(dormitoryFloorVO.getRoomLocation());
        return dormitoryFloorVO;
    }
    private String getFloorRoom(Integer floor,Integer room){
        return String.valueOf(floor*100+room);
    }


    @Override
    public void verifyUserUuid(String userUuid, HttpServletResponse response) {
        Jedis resource = jedisPool.getResource();
        String userKey = String.format(User_SignUp_Key, userUuid);
        Boolean ifExist = resource.exists(userKey);
        if(ifExist){
            // 如果存在表示用户已经输入过信息表单，现在正在完成登录校验
            UserSignUpDTO userSignUpDTO = JSON.parseObject(resource.get(userKey), UserSignUpDTO.class);
            log.error(userSignUpDTO.toString());
            UtilityBillUserDTOPO utilityBillUserDTOPO = utilityBillConvert.userSignUpDTOToUtilityBillUserDTOPO(userSignUpDTO);

            String dormitoryIdKey = userSignUpDTO.getDormitoryId();

            String dormitoryId = String.valueOf(dormitoryCodeDao.selectCodeId(dormitoryIdKey));

            utilityBillUserDTOPO.setDormitoryRoomId(
                    Integer.valueOf(
                            dormitoryId.concat(userSignUpDTO.getDormitoryRoomId())
                    )
            );
            utilityBillUserDTOPO.setDormitoryId(Integer.valueOf(dormitoryId));
            utilityBillUserService.save(utilityBillUserDTOPO);


        }

    }
}

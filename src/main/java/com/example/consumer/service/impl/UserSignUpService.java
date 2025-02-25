package com.example.consumer.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.consumer.config.JwtProperties;
import com.example.consumer.config.properties.UserVerifyProperties;
import com.example.consumer.convert.UtilityBillConvert;
import com.example.consumer.dao.DormitoryCodeDao;
import com.example.consumer.dao.UserDao;
import com.example.consumer.exception.BizException;
import com.example.consumer.mapper.UniversityCodeMapper;
import com.example.consumer.mapper.UtilityBillUserMapper;
import com.example.consumer.pojo.dto.*;
import com.example.consumer.pojo.entity.UserSignUpEnum;
import com.example.consumer.pojo.po.*;
import com.example.consumer.pojo.vo.DormitoryBuildingVO;
import com.example.consumer.pojo.vo.DormitoryFloorVO;
import com.example.consumer.pojo.vo.UniversityInformationListVO;
import com.example.consumer.service.IUserSignUpService;
import com.example.consumer.utils.JwtTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private UniversityCodeMapper universityCodeMapper;
    @Resource
    public JwtTool jwtTool;

    @Resource
    private MailSendingService mailSendingService;
    @Resource
    private UtilityBillConvert utilityBillConvert;

    @Resource
    private UserVerifyProperties userVerifyProperties;
    @Resource
    private JedisPool jedisPool;

    @Resource
    private DormitoryCodeDao dormitoryCodeDao;

    @Resource
    private UtilityBillUserService utilityBillUserService;

    @Resource
    private UserService userService;

    @Resource
    private UniversityCodeService universityCodeService;

    @Resource
    private UserDao userDao;


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
    private static final String USER_SIGNUP_KEY = "user:signUp:%s";
    private static final String USER_SIGNUP_EMAIL_UNIQUE_KEY = "user:signUp:email:%s";
    private static final String USER_SIGNUP_EMAIL_LIST_KEY = "user:signUp:list:%s";


    @Override
    public UniversityInformationListVO getUniversityInformation() {
        List<UniversityCodePO> universityAndArea = universityCodeService.list();

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
        UserPO userPOByEmail = userDao.getByEmail(userSignUpDTO.getEmail());
        // 如果还没有注册就发送邮件
        if (Objects.isNull(userPOByEmail)) {
            // 16位的uuid
            UUID uuid = UUID.randomUUID();
            String fullUUID = uuid.toString().replace("-", "");
            String userUuid = fullUUID.substring(0, 16);
            try (Jedis resource = jedisPool.getResource()) {
                long exSeconds = Minutes_5.getSeconds();
                // 邮件key 用于邮件的幂等校验
                // 用户注册信息的key 用于缓存用户信息
                // emailKey 永远唯一 但是userKey不一定 emailKey与userKey是1对多的关系
                // 用户注册时可以多次更新用户信息 最终多个连接指向同一个用户注册信息
                String emailKey = String.format(USER_SIGNUP_EMAIL_UNIQUE_KEY, userSignUpDTO.getEmail());
                String userKey = String.format(USER_SIGNUP_KEY, userUuid);
                String emailLinkListKey = String.format(USER_SIGNUP_EMAIL_LIST_KEY, userSignUpDTO.getEmail());

                resource.lpush(emailLinkListKey, userUuid);
                resource.setex(emailKey, exSeconds, getEmailToUserNum(resource, emailKey));
                resource.setex(userKey, exSeconds, JSON.toJSONString(userSignUpDTO));

                mailSendingService.sendHtmlMailFormQQMail(userSignUpDTO.getEmail(), userSignUpDTO.getUserName(), userUuid);
            } catch (Exception exception) {
                log.error("邮件发送失败", exception);
            }
            userSignUpRespDTO.setVerifyCode(UserSignUpEnum.USER_NEVER_SIGNUP.getValue());
            userSignUpRespDTO.setSignUpMsg(UserSignUpEnum.USER_NEVER_SIGNUP.getSignUpMsg());
            userSignUpRespDTO.setInformMsg("登录校验邮件已经发送");
            userSignUpRespDTO.setUserUuid(userUuid);
        } else {
            // 否则表示已经注册了 表示可以直接跳转到到登录页面
            userSignUpRespDTO.setVerifyCode(UserSignUpEnum.USER_HAS_SIGNUP.getValue());
            userSignUpRespDTO.setSignUpMsg(UserSignUpEnum.USER_HAS_SIGNUP.getSignUpMsg());
            userSignUpRespDTO.setInformMsg("登录校验邮件未发送");
            userSignUpRespDTO.setUserUuid("");

        }
        return userSignUpRespDTO;
    }

    private String getEmailToUserNum(Jedis resource, String emailKey) {
        int result = 0;
        if (resource.exists(emailKey)) {
            // 如果存在就取数据++ 否则为0
            int originalNum = Integer.parseInt(resource.get(emailKey));
            originalNum++;
            result = originalNum;
        }
        return String.valueOf(result);
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
                floorDTO.setFloorRoomShow(getFloorRoom(floorNum, roomNum));

                floorsList.add(floorDTO);
            }
            floorToRoomMap.put(String.valueOf(floorNum), floorsList);
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

    private String getFloorRoom(Integer floor, Integer room) {
        return String.valueOf(floor * 100 + room);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyUserUuid(String userUuid, HttpServletResponse response) {
        String userKey = String.format(USER_SIGNUP_KEY, userUuid);
        try (Jedis resource = jedisPool.getResource()) {
            Boolean ifExist = resource.exists(userKey);
            if (ifExist) {
                // 如果存在表示用户已经输入过信息表单，现在正在完成登录校验
                UserSignUpDTO userSignUpDTO = JSON.parseObject(resource.get(userKey), UserSignUpDTO.class);
                String emailKey = String.format(USER_SIGNUP_EMAIL_UNIQUE_KEY, userSignUpDTO.getEmail());
                String emailLinkListKey = String.format(USER_SIGNUP_EMAIL_LIST_KEY, userSignUpDTO.getEmail());
                if (!resource.exists(emailKey)) {
                    // 如果不存在key 就直接返回 表示用户数据已经插入过了
                    return;
                }
                // 从栈空间中弹出一个uuid key 实现幂等校验
                String latestUserUuidKey = resource.lpop(emailLinkListKey);
                if (!latestUserUuidKey.equals(userUuid)) {
                    // 如果最新的uuid的key 和 当前的userUuid不相当 表示 改邮箱表示的用户有多次提交记录
                    // 则需要最新的这个userUuid的key 才能正确获取到用户数据
                    resource.del(userKey);
                    userKey = String.format(USER_SIGNUP_KEY, latestUserUuidKey);
                    userSignUpDTO = JSON.parseObject(resource.get(userKey), UserSignUpDTO.class);
                }

                String dormitoryIdKey = userSignUpDTO.getDormitoryId();
                String dormitoryId = String.valueOf(dormitoryCodeDao.selectCodeId(dormitoryIdKey));

                UtilityBillUserDTOPO utilityBillUserDTOPO = utilityBillConvert.userSignUpDTOToUtilityBillUserDTOPO(userSignUpDTO);
                utilityBillUserDTOPO.setDormitoryRoomId(
                        Integer.valueOf(
                                dormitoryId.concat(userSignUpDTO.getDormitoryRoomId())
                        )
                );
                utilityBillUserDTOPO.setDormitoryId(Integer.valueOf(dormitoryId));
                // 用户数据插入表
                // 理论来说 此时不应该存在一个用户多次点击链接 重复插表
                UserPO userPO = this.saveUserIntoDB(userSignUpDTO, utilityBillUserDTOPO);

                resource.del(userKey);
                resource.del(emailKey);
                resource.del(emailLinkListKey);

                // 页面重定向 同时传送token
                String key = jwtTool.createToken(userPO.getUuid(), JwtProperties.tokenTTL);
                response.sendRedirect(String.format(userVerifyProperties.getSendRedirect(), key));
            }
        } catch (Exception exception) {
            log.error(exception.toString(), exception);
            throw new BizException(exception.getMessage(), 101001);
        }
    }

    private UserPO saveUserIntoDB(UserSignUpDTO userSignUpDTO, UtilityBillUserDTOPO utilityBillUserDTOPO) {
        UserPO userPO = utilityBillConvert.userSignUpDTOToUserPO(userSignUpDTO);
        String email = userPO.getEmail().trim();
        String password = userPO.getPassword().trim();
        String userName = userPO.getUserName().trim();

        userPO.setEmail(email);
        userPO.setPassword(password);
        userPO.setUserName(userName);

        userService.save(userPO);
        utilityBillUserService.save(utilityBillUserDTOPO);
        return userPO;
    }
}

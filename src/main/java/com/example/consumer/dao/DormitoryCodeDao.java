package com.example.consumer.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.consumer.mapper.DormitoryAreaMapper;
import com.example.consumer.mapper.DormitoryCodeMapper;
import com.example.consumer.pojo.po.DormitoryAreaPO;
import com.example.consumer.pojo.po.DormitoryCodePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DormitoryCodeDao {
    private final DormitoryCodeMapper dormitoryCodeMapper;
    private final DormitoryAreaMapper dormitoryAreaMapper;


    /**
     * 根据university code 查对应学校的宿舍信息 宿舍区和楼
     *
     * @param code:
     * @return java.util.List<com.example.consumer.pojo.po.DormitoryCodePO>
     */

    public List<DormitoryCodePO> selectListByUniversityCode(String code){
        LambdaQueryWrapper<DormitoryCodePO> wrapper = Wrappers.lambdaQuery();

        wrapper.select(DormitoryCodePO::getDormitoryAreaUuid,
                        DormitoryCodePO::getName,
                        DormitoryCodePO::getCodeId,
                        DormitoryCodePO::getUuid)
                .eq(DormitoryCodePO::getUniversityCodeId,code);
        return dormitoryCodeMapper.selectList(wrapper);
    }

    public Map<String,DormitoryAreaPO> selectMapDormitoryUuidToName(String universityCode){
        LambdaQueryWrapper<DormitoryAreaPO> wrapper = Wrappers.lambdaQuery();
        wrapper.select(DormitoryAreaPO::getUuid,DormitoryAreaPO::getName)
                .eq(DormitoryAreaPO::getUniversityCode,universityCode);
        return dormitoryAreaMapper.selectList(wrapper)
                .stream()
                .collect(Collectors.toMap(DormitoryAreaPO::getUuid,i->i));
    }

    public Long selectCodeId(String dormitoryUuid){
        LambdaQueryWrapper<DormitoryCodePO> wrapper = Wrappers.lambdaQuery();
        wrapper.select(DormitoryCodePO::getCodeId)
                .eq(DormitoryCodePO::getUuid,dormitoryUuid);
        DormitoryCodePO dormitoryCodePO = dormitoryCodeMapper.selectOne(wrapper);
        return dormitoryCodePO.getCodeId();
    }



}

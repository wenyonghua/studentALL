package com.waitig.mapper;

import com.waitig.entity.DrawLevelEntity;
import com.waitig.entity.DrawStaffEntity;
import com.waitig.entity.PCodeEntity;
import com.waitig.entity.StaffEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface IDrawMapper {
    List<PCodeEntity> getOption(Map<String, Object> paramMap);

    List<DrawLevelEntity> getDrawInfo();

    List<DrawStaffEntity> getDrawStaff();

    List<StaffEntity> getNotDrawStaffList();

    void addDrawStaff(DrawStaffEntity drawStaffEntity);

    void archiveDrawStaffLog();

    void delDrawStaff();

    List<StaffEntity> getAllStaff();

    void delDrawStaffByStaff(DrawStaffEntity drawStaffEntity);
}

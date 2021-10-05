package com.waitig.service;

import com.waitig.entity.DrawLevelEntity;
import com.waitig.entity.DrawStaffEntity;
import com.waitig.entity.PCodeEntity;
import com.waitig.entity.StaffEntity;
import com.waitig.mapper.IDrawMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class DrawService {

    @Autowired
    IDrawMapper iDrawMapper;

    Logger logger = Logger.getLogger(this.getClass().getName());

    public List<PCodeEntity> getOption(Map<String, Object> paramMap) {
        return iDrawMapper.getOption(paramMap);
    }

    public List<DrawLevelEntity> getDrawInfo() {
        return iDrawMapper.getDrawInfo();
    }

    /**
     * 抽奖主要逻辑
     *
     * @param paramMap
     * @return
     */
    public StaffEntity draw(Map<String, Object> paramMap) {
        logger.info("开始抽奖。");
        logger.info(paramMap.toString());
        // 获取所有未抽奖的人员信息
        List<StaffEntity> staffEntityList = iDrawMapper.getNotDrawStaffList();
        int size = staffEntityList.size();
        Random random = new Random();
        int no = random.nextInt(size);
        StaffEntity luckyStaff = staffEntityList.get(no);
        while (!this.checkDrawStaff(luckyStaff)) {
            no = random.nextInt(size);
            luckyStaff = staffEntityList.get(no);
        }
        logger.info("幸运儿已经产生：" + luckyStaff.toString());
        DrawStaffEntity drawStaffEntity = new DrawStaffEntity();
        drawStaffEntity.setLevel(paramMap.get("drawLevel").toString());
        drawStaffEntity.setStaffId(luckyStaff.getStaffId());
        iDrawMapper.addDrawStaff(drawStaffEntity);
        return luckyStaff;
    }

    public List<DrawStaffEntity> getDrawStaff() {
        return iDrawMapper.getDrawStaff();
    }

    @Transactional
    public String resetDrawStaff() {
        // 归档历史数据
        iDrawMapper.archiveDrawStaffLog();
        iDrawMapper.delDrawStaff();
        return "操作成功！";
    }

    /**
     * 判断抽奖数据是否符合规则，比如手机号是否连号等等
     *
     * @param luckyStaff
     * @return
     */
    public boolean checkDrawStaff(StaffEntity luckyStaff) {
        // 获奖人员不能与上次获奖人员连号，需排除
        List<DrawStaffEntity> drawStaffEntityList = this.getDrawStaff();
        if (drawStaffEntityList.size() < 1) {
            return true;
        }
        DrawStaffEntity lastDrawStaff = drawStaffEntityList.get(drawStaffEntityList.size() - 1);
        double luckyNum = Double.parseDouble(luckyStaff.getStaffMobile());
        double lastNum = Double.parseDouble(lastDrawStaff.getStaffMobile());
        logger.info("luckyNum:" + luckyNum + ";lastNum:" + lastNum);
        try {
            double i = luckyNum - lastNum;
            if (i == 1.0 || i == -1.0) {
                logger.info("本次抽奖与上次抽奖连号了！");
                logger.info("本次抽奖信息：" + luckyStaff.toString());
                logger.info("上次抽奖信息：" + lastDrawStaff.toString());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public List<StaffEntity> getAllStaff() {
        return iDrawMapper.getAllStaff();
    }

    public String delDrawStaff(DrawStaffEntity drawStaffEntity) {
        iDrawMapper.delDrawStaffByStaff(drawStaffEntity);
        return "操作成功！";
    }
}

package com.waitig.controller;

import com.waitig.entity.DrawLevelEntity;
import com.waitig.entity.DrawStaffEntity;
import com.waitig.entity.PCodeEntity;
import com.waitig.entity.StaffEntity;
import com.waitig.service.DrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@CrossOrigin
@RequestMapping(value = "/draw")
public class DrawController {

    Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    DrawService drawService;

    @PostMapping(value = "/getOption")
    public List<PCodeEntity> getOption(@RequestBody Map<String,Object> paramMap) {
        return drawService.getOption(paramMap);
    }

    @GetMapping(value = "/getDrawInfo")
    public List<DrawLevelEntity> getDrawInfo() {
        return drawService.getDrawInfo();
    }

    @PostMapping(value = "/draw")
    public StaffEntity draw(@RequestBody Map<String,Object> paramMap) {
        return drawService.draw(paramMap);
    }

    @GetMapping(value = "/getDrawStaff")
    public List<DrawStaffEntity> getDrawStaff() {
        return drawService.getDrawStaff();
    }

    @PostMapping(value = "/resetDrawStaff")
    public String resetDrawStaff() {
        return drawService.resetDrawStaff();
    }

    @GetMapping(value = "/getAllStaff")
    public List<StaffEntity> getAllStaff() {
        return drawService.getAllStaff();
    }

    @PostMapping(value = "/delDrawStaff")
    public String delDrawStaff(@RequestBody DrawStaffEntity drawStaffEntity) {
        return drawService.delDrawStaff(drawStaffEntity);
    }

}

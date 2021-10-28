package me.zohar.runscore.dictconfig.controller;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.dictconfig.DictHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/dictconfig")
public class DictConfigController {

    @GetMapping("/findDictItemInCache")
    @ResponseBody
    public Result findDictItemInCache(String dictTypeCode) {
        return Result.success().setData(DictHolder.findDictItem(dictTypeCode));
    }

}

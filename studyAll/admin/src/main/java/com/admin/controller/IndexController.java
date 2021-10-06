package com.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // 可以被视图解析器解析
public class IndexController {
    // 只要访问了/ 就会跳转到 templates目录下的 index 页面
    // SpringBoot 要使用默认的模板引擎 thymeleaf 一定需要导入其对应的依赖！
    @RequestMapping("/index")
    public String index(){
        /* System.out.println("1111"); */
        return "index";
    }

    @RequestMapping("/")
    public String hello(){
        return "dad";
    }
}

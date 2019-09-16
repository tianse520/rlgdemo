package com.itdr.controllers.portal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/aaa.do")
    public void a(){
        System.out.println("ok");
    }
}

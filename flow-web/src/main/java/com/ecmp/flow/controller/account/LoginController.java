package com.ecmp.flow.controller.account;

import com.ecmp.core.util.SystemUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by fly on 2017/3/29.
 */
@Controller
@RequestMapping(value = "/login")
public class LoginController {

    @RequestMapping(method = RequestMethod.GET,value = "user")
    public String login(String cashLogin) {
        Subject subject = (Subject) SecurityUtils.getSubject();
        if (subject != null && subject.isAuthenticated()) {// 已经登录了，不允许再来登录
            return "redirect:/";
        }
        return "account/login";
    }

}

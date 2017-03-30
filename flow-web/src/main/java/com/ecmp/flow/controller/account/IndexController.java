/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package com.ecmp.flow.controller.account;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * LoginController负责打开登录页面(GET请求)和登录出错页面(POST请求)，
 * <p>
 * 真正登录的POST请求由Filter完成,
 *
 * @author calvin
 */
@Controller
@RequestMapping(value = "/index")
public class IndexController {


    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return "account/home";
    }


}

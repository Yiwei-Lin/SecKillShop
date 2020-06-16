package com.shop.controller;

import com.shop.message.ErrorMsg;
import com.shop.message.ResultMsg;
import com.shop.redis.RedisService;
import com.shop.service.UserService;
import com.shop.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/")
public class LoginController {

	private static Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
    UserService userService;
	
	@Autowired
    RedisService redisService;
	
    @GetMapping("/login")
    public String toLogin() {
        return "login";
    }

    @PostMapping("/login")
    public ResultMsg<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        logger.info(loginVo.toString());
        //登录
        boolean success = userService.login(response, loginVo);
        if(success)
            return ResultMsg.success(true);
        else
            return ResultMsg.error(ErrorMsg.LOGIN_ERROR);
    }

    // 压测用
    @PostMapping("/login_for_test")
    public ResultMsg<String> doTestLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        logger.info(loginVo.toString());
        //登录
        String token = userService.loginForTest(response, loginVo);
        return ResultMsg.success(token);
    }
}

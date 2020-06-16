package com.shop.service;

import com.shop.dao.UserDao;
import com.shop.entity.User;
import com.shop.exception.GlobalException;
import com.shop.message.ErrorMsg;
import com.shop.redis.RedisService;
import com.shop.redis.prefix.UserPrefix;
import com.shop.utils.MD5Util;
import com.shop.utils.UUIDUtil;
import com.shop.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {

    public static final String COOKIE_TOKEN = "sec_kill_shop_token";

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisService redisService;

    /**
     * 优化：增加用户对象缓存
     * 在对user操作时：修改用户信息等，需要判断是否缓存，删除缓存
     */
    public User getById(long id) {

        User user = (User) redisService.get(UserPrefix.ID_PREFIX, String.valueOf(id), User.class);
        if(user != null) {
            return user;
        }
        user = userDao.getById(id);
        if(user != null) {
            redisService.set(UserPrefix.ID_PREFIX, String.valueOf(id), user);
        }
        return user;
    }

    public User getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        User user = (User) redisService.get(UserPrefix.TOKEN_PREFIX, token, User.class);
        // 延长有效期
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null) {
            throw new GlobalException(ErrorMsg.LOGIN_ERROR);
        }
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //判断手机号是否存在
        User user = getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(ErrorMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String correctPassword = user.getPassword();
        String salt = user.getSalt();
        if(!MD5Util.md5WithSalt(password, salt).equals(correctPassword)) {
            throw new GlobalException(ErrorMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;
    }

    public String loginForTest(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null) {
            throw new GlobalException(ErrorMsg.LOGIN_ERROR);
        }
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //判断手机号是否存在
        User user = getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(ErrorMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String correctPassword = user.getPassword();
        String salt = user.getSalt();
        if(!MD5Util.md5WithSalt(password, salt).equals(correctPassword)) {
            throw new GlobalException(ErrorMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    private void addCookie(HttpServletResponse response, String token, User user) {
        redisService.set(UserPrefix.TOKEN_PREFIX, token, user);
        Cookie cookie = new Cookie(COOKIE_TOKEN, token);
        cookie.setMaxAge(UserPrefix.TOKEN_PREFIX.getExpireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}

package com.good.zdisksystem.controller;

import cn.hutool.core.bean.BeanUtil;
import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.cache.AuthUserCache;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.constant.AuthCacheConstant;
import com.good.zdisksystem.entity.vo.UserInfoVo;
import com.good.zdisksystem.security.utils.JwtUtils;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.param.LoginRequest;
import com.good.zdisksystem.entity.param.RegisterRequest;
import com.good.zdisksystem.entity.vo.AuthResponse;
import com.good.zdisksystem.entity.vo.CaptchaVO;
import com.good.zdisksystem.service.UserService;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private Producer captchaProducer;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserService usersService;
    @Autowired
    private AuthUserCache authUserCache;

    /**
     * 获取验证码
     *
     * @return
     */
    @GetMapping("/captcha")
    public CommonResult<CaptchaVO> getCaptcha() {
        // 生成验证码文本
        String capText = captchaProducer.createText();
        // 生成验证码图片
        BufferedImage image = captchaProducer.createImage(capText);
        // 生成uuid作为验证码的key
        String uuid = UUID.randomUUID().toString();
        // 将验证码存入Redis，设置5分钟过期
        authUserCache.setCaptcha(uuid, capText);
        // 将图片转为Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        CaptchaVO captchaVO = new CaptchaVO(uuid, "data:image/jpeg;base64," + base64Image);
        return CommonResult.success(captchaVO);
    }

    /**
     * 登录
     *
     * @param loginRequest
     * @return
     */
    @PostMapping("/login")
    @OperationLogger(module = "登录", action = "登录", detail = "用户登录")
    public CommonResult<AuthResponse> login(@RequestBody @Validated LoginRequest loginRequest) {
        // 验证验证码
        String captcha = authUserCache.getCaptcha(loginRequest.getUuid());
        if (captcha == null) {
            return CommonResult.error("验证码已过期");
        }
        if (!captcha.equalsIgnoreCase(loginRequest.getCaptcha())) {
            return CommonResult.error("验证码错误");
        }
        // 验证成功后删除验证码
        authUserCache.delCaptcha(loginRequest.getUuid());
        AuthResponse response = usersService.login(loginRequest);
        return CommonResult.success(response);
    }

    /**
     * 注册用户
     *
     * @param registerRequest 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public CommonResult<UserInfoVo> register(@RequestBody @Validated RegisterRequest registerRequest) {
        // 验证验证码
        String captcha = authUserCache.getCaptcha(registerRequest.getUuid());
        if (captcha == null) {
            return CommonResult.error("验证码已过期");
        }
        if (!captcha.equalsIgnoreCase(registerRequest.getCaptcha())) {
            return CommonResult.error("验证码错误");
        }
        // 验证成功后删除验证码
        authUserCache.delCaptcha(registerRequest.getUuid());
        User user = usersService.register(registerRequest);
        UserInfoVo userInfoVo = BeanUtil.toBean(user, UserInfoVo.class);
        return CommonResult.success(userInfoVo);
    }

    /**
     * 获取当前用户信息
     *
     * @return
     */
    @GetMapping("/info")
    public CommonResult<UserInfoVo> getUserInfo() {
        User currentUser = RequestUser.getUser();
        currentUser.setRoles(usersService.getUserRoles(currentUser.getId()));
        UserInfoVo userInfoVo = BeanUtil.toBean(currentUser, UserInfoVo.class);
        return CommonResult.success(userInfoVo);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/logout")
    @OperationLogger(module = "登录", action = "退出登录", detail = "用户退出登录")
    public CommonResult<?> logout() {
        User user = RequestUser.getUser();
        if (user == null) {
            return CommonResult.error("用户未登录");
        }
        // 清楚缓存中的token信息
        authUserCache.delAccessToken(user.getPhone());
        // 清理当前用户信息
        RequestUser.remove();
        return CommonResult.success("退出成功");
    }

}

package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.CaptchaConstant;
import com.cjym.videoweb.enums.ResultEnum;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.model.dto.UserDto;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.UserInfoService;
import io.github.eternalstone.captcha.gp.base.TextEntry;
import io.github.eternalstone.captcha.gp.captcha.SpecCaptcha;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AuthenticationManager authenticationManager;

    /**
     * 生成验证码
     * @param captchaType 验证码类型
     * @param email 邮箱（非必填）
     * */
    @Override
    public R<CaptchaVo> getCaptcha(String captchaType, String email) {
        // 1. 创建验证码对象 (宽度, 高度, 字符数)
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 6);
        String captchaKey = UUID.randomUUID().toString();
        TextEntry text = captcha.createText();
        String code = text.charsText();
        stringRedisTemplate.opsForValue().set(captchaKey,code,5, TimeUnit.MINUTES);
        if(CaptchaConstant.IMG_CAPTCHA.equals(captchaType)){
            // 2. 获取验证码文本和Base64图片
            String base64Image = captcha.toBase64(text);
            return R.success(CaptchaVo.builder().key(captchaKey).captcha(base64Image).build());
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            // 邮箱格式正确，发送验证码
            return R.success(CaptchaVo.builder().key(captchaKey).build());
        }
        throw new GlobeException(ResultEnum.EMAIL_ERROR.getMessage());
    }

    /**
     * 用户登录
     * @param userLoginVo 登录信息
     * */
    @Override
    public void login(UserLoginVo userLoginVo) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginVo.getUsername(),
                        userLoginVo.getPassword()
                )
        );
        UserDto user = (UserDto) authentication.getPrincipal();
        //todo 生成token
        System.out.println("登录成功");
    }
}

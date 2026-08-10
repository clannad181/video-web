package com.cjym.videoweb.provider;

import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.encoders.CustomPasswordEncoder;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.dto.UserDto;
import com.cjym.videoweb.service.impl.CustomUserDetailsServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Resource
    private CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private CustomPasswordEncoder customPasswordEncoder;

    /**
     * 实际登录校验逻辑
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //获取用户名
        String username = authentication.getName();
        //获取密码
        String password = authentication.getCredentials().toString();

        // 从 details 中读取登录系统标识："front" → type=0 用户，其他 → type=1 用户
        Object details = authentication.getDetails();
        boolean isFront = BaseConstant.SYS_FRONT.equals(details);

        UserDto user;
        if (isFront) {
            user = userInfoMapper.selectFrontDtoByUsername(username);
            if (user == null) {
                throw new GlobeException("账号或密码错误");
            }
        } else {
            user = (UserDto) customUserDetailsServiceImpl.loadUserByUsername(username);
        }
        // 2. 自定义状态检查
        if (!user.isEnabled()) {
            throw new GlobeException("账户已禁用");
        }
        // 密码加盐传输
        password= user.getSalt() + BaseConstant.SALT_SPITE_IDX + password;
        // 3. 密码校验
        if (!customPasswordEncoder.matches(password, user.getPassword())) {
            // 这里可以记录登录失败次数等逻辑
            throw new GlobeException("密码错误");
        }

        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

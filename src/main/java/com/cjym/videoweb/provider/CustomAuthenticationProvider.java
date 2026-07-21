package com.cjym.videoweb.provider;

import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.encoders.CustomPasswordEncoder;
import com.cjym.videoweb.exception.GlobeException;
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
        UserDto user = (UserDto) customUserDetailsServiceImpl.loadUserByUsername(username);

        // 2. 自定义状态检查
        if (!user.isEnabled()) {
            throw new GlobeException("账户已禁用");
        }
        // 可继续添加其他检查：isAccountNonLocked、isAccountNonExpired 等
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

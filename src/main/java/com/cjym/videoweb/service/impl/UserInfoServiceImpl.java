package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.constant.CaptchaConstant;
import com.cjym.videoweb.enums.ResultEnum;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.model.dto.UserDto;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.PasswordVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.EmailSendService;
import com.cjym.videoweb.service.UserInfoService;
import com.cjym.videoweb.utils.JWTUtil;
import com.cjym.videoweb.utils.MD5Util;
import io.github.eternalstone.captcha.gp.base.TextEntry;
import io.github.eternalstone.captcha.gp.captcha.SpecCaptcha;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Set;
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

    @Resource
    private EmailSendService emailSendService;

    private static final String fromEmail="2657463589@qq.com";
    private UserInfoService userInfoService;

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
        if(CaptchaConstant.ADMIN_IMG_CAPTCHA.equals(captchaType)){
            // 2. 获取验证码文本和Base64图片
            String base64Image = captcha.toBase64(text);
            stringRedisTemplate.opsForValue().set(CaptchaConstant.ADMIN_IMG_CAPTCHA +captchaKey,code.toLowerCase(),5, TimeUnit.MINUTES);
            return R.success(CaptchaVo.builder().key(captchaKey).captcha(base64Image).build());
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            // 邮箱格式正确，发送验证码
            UserDto userDto = baseMapper.selectDtoByEmail(email);
            if (ObjectUtils.isEmpty(userDto)) {
                throw new GlobeException(ResultEnum.EMAIL_ERROR.getMessage());
            }
            emailSendService.send(fromEmail, email, fromEmail, "验证码", "【cjym】本次的验证码为：" + code + "\n在15分钟内有效,如非本人操作,请忽略此条邮件");
            stringRedisTemplate.opsForValue().set(CaptchaConstant.ADMIN_EMAIL_CAPTCHA +captchaKey,code.toLowerCase(),5, TimeUnit.MINUTES);
            return R.success(CaptchaVo.builder().key(captchaKey).build());
        }
        throw new GlobeException(ResultEnum.EMAIL_ERROR.getMessage());
    }

    /**
     * 用户登录
     * @param userLoginVo 登录信息
     * */
    @Override
    public R<Map<String,Object>> login(UserLoginVo userLoginVo) {
        UserDto user = null;
        String captChaKey;
        if(CaptchaConstant.ADMIN_IMG_CAPTCHA.equals(userLoginVo.getLoginType())){
            captChaKey= CaptchaConstant.ADMIN_IMG_CAPTCHA +userLoginVo.getKey();
            String redisCaptcha = stringRedisTemplate.opsForValue().getAndDelete(captChaKey);
            if(StringUtils.isEmpty(redisCaptcha)||!redisCaptcha.equalsIgnoreCase(userLoginVo.getCaptcha().toLowerCase())||StringUtils.isEmpty(userLoginVo.getCaptcha())){
                throw new GlobeException(ResultEnum.CAPTCHA_EXPIRE.getMessage());
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginVo.getUsername(),
                            userLoginVo.getPassword().toLowerCase()
                    )
            );
            user = (UserDto) authentication.getPrincipal();
        }else{
            captChaKey= CaptchaConstant.ADMIN_EMAIL_CAPTCHA +userLoginVo.getKey();
            String redisCaptcha = stringRedisTemplate.opsForValue().getAndDelete(captChaKey);
            if(StringUtils.isEmpty(redisCaptcha)||!redisCaptcha.equalsIgnoreCase(userLoginVo.getCaptcha().toLowerCase())||StringUtils.isEmpty(userLoginVo.getCaptcha())){
                throw new GlobeException(ResultEnum.CAPTCHA_EXPIRE.getMessage());
            }
            user = baseMapper.selectDtoByEmail(userLoginVo.getEmail());
            if (!user.isEnabled()) {
                throw new GlobeException(ResultEnum.USER_DISABLE.getMessage());
            }
        }
        String accessToken = JWTUtil.createAccessToken(BaseConstant.SYS_ADMIN, user.getUsername(), user.getAuthorities());
        String refreshToken = JWTUtil.createRefreshToken(BaseConstant.SYS_ADMIN, user.getUsername());
        saveRefreshToken(refreshToken,user.getUsername());
        return R.success(Map.of("accessToken",accessToken,"refreshToken",refreshToken));
    }

    @Override
    public R<UserDto> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = JWTUtil.getUsername(token);
        return R.success(baseMapper.selectDtoByUsername(username));
    }

    @Override
    public R<Map<String, Object>> refresh(HttpServletRequest request) {
        String requestRefreshToken = request.getHeader("refresh-token");
        // 1. 验证 Refresh Token 是否有效
        if (verifyRefreshToken(requestRefreshToken)) {
            return R.error(ResultEnum.FORBIDDEN_ERROR);
        }
        // 2. 从 JWT 中获取用户名
        String username = JWTUtil.getUsername(requestRefreshToken);
        // 3. 撤销旧 Refresh Token（Rotation：防止旧 token 继续使用）
        deleteByToken(requestRefreshToken);
        // 4. 生成新的 Access Token 和 Refresh Token
        UserDto userDetails = baseMapper.selectDtoByUsername(username);
        String newAccessToken = JWTUtil.createAccessToken(BaseConstant.SYS_ADMIN,username, userDetails.getAuthorities());
        String newRefreshToken = JWTUtil.createRefreshToken(BaseConstant.SYS_ADMIN,username);
        // 5. 返回新的 Token 对
        return R.success(Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));
    }

    @Override
    public R<String> resetPassword(PasswordVo passwordVo) {
        Long userId = passwordVo.getUserId();
        String username = passwordVo.getUsername();
        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserInfo::getId, userId);
        lambdaQueryWrapper.eq(UserInfo::getUserName, username);
        UserInfo userInfo = baseMapper.selectOne(lambdaQueryWrapper);
        if(ObjectUtils.isEmpty(userInfo)){
            throw new GlobeException("用户不存在");
        }
        String oldPassword = MD5Util.md5WithSalt(passwordVo.getOldPassword(), userInfo.getSalt());
        if(!userInfo.getPassword().equals(oldPassword)){
            throw new GlobeException("旧密码错误");
        }
        //重新生成盐值
        String salt = MD5Util.generateSalt();
        //重新加密
        String newPassword = MD5Util.md5WithSalt(passwordVo.getNewPassword(), salt);
        userInfo.setSalt(salt);
        userInfo.setPassword(newPassword);
        baseMapper.updateById(userInfo);
        return R.success("密码重置成功");
    }

    public void saveRefreshToken(String refreshToken,String username) {
        // 计算 token 的剩余有效期（毫秒）
        long expiration = JWTUtil.parseClaims(refreshToken).getExpiration().getTime() - System.currentTimeMillis();
        String userZSetKey = BaseConstant.USER_TOKENS_ZSET_PREFIX + username;
        // 2. 清理 ZSet 中已过期的 token（可选，确保计数准确）
        cleanExpiredTokens(userZSetKey);
        // 3. 检查该用户的当前有效会话数
        Long currentSize = stringRedisTemplate.opsForZSet().size(userZSetKey);
        if (currentSize != null && currentSize >= BaseConstant.MAX_SESSIONS) {
            // 获取最旧的一个 token（score 最小）
            Set<String> oldestTokenSet = stringRedisTemplate.opsForZSet().range(userZSetKey, 0, 0);
            if (oldestTokenSet != null && !oldestTokenSet.isEmpty()) {
                String oldestToken = oldestTokenSet.iterator().next();
                // 删除最旧的 Refresh Token 记录
                stringRedisTemplate.delete(BaseConstant.REFRESH_TOKEN_PREFIX + oldestToken);
                // 从 ZSet 中移除该 token
                stringRedisTemplate.opsForZSet().remove(userZSetKey, oldestToken);
            }
        }
        // 4. 存储新 Token：refresh_token:xxxxx -> username
        stringRedisTemplate.opsForValue().set(BaseConstant.REFRESH_TOKEN_PREFIX + refreshToken, username, expiration, TimeUnit.MILLISECONDS);
        // 5. 将新 Token 加入用户 ZSet，score 为当前时间戳
        stringRedisTemplate.opsForZSet().add(userZSetKey, refreshToken, System.currentTimeMillis());
        // 这里设置一个较长的时间，保证不会无限增长
        stringRedisTemplate.expire(userZSetKey, expiration + 60000, TimeUnit.MILLISECONDS);
    }

    private void cleanExpiredTokens(String userZSetKey) {
        long now = System.currentTimeMillis();
        long refreshValidity = JWTUtil.getRefreshValidity();
        long minScore = now - refreshValidity;
        // 移除所有 score 小于 minScore 的成员（即过期的 token）
        stringRedisTemplate.opsForZSet().removeRangeByScore(userZSetKey, 0, minScore);
    }


    /**
     * 验证 Refresh Token 是否有效
     */
    public boolean verifyRefreshToken(String token) {
        // JWT 本身必须合法
        if (!JWTUtil.validateToken(token)) return false;
        if (!BaseConstant.TOKEN_REFRESH.equals(JWTUtil.getTokenType(token))) return false;
        // Redis 中必须存在
        String key = BaseConstant.REFRESH_TOKEN_PREFIX + token;
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 退出登录时删除 Refresh Token
     */
    public void deleteByUsernameAndToken(String username, String token) {
        stringRedisTemplate.delete(BaseConstant.REFRESH_TOKEN_PREFIX + token);
        stringRedisTemplate.opsForZSet().remove(BaseConstant.USER_TOKENS_ZSET_PREFIX + username, token);
    }

    /**
     * 刷新时删除旧 Refresh Token（轮换）
     */
    public void deleteByToken(String token) {
        String key = BaseConstant.REFRESH_TOKEN_PREFIX + token;
        String username = stringRedisTemplate.opsForValue().get(key);
        if (username != null) {
            // 从 ZSet 中移除
            stringRedisTemplate.opsForZSet().remove(BaseConstant.USER_TOKENS_ZSET_PREFIX + username, token);
        }
        // 删除 token 本身
        stringRedisTemplate.delete(key);
    }
}

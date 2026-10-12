package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.annotation.LogLogin;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.constant.CaptchaConstant;
import com.cjym.videoweb.enums.ResultEnum;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.FollowMapper;
import com.cjym.videoweb.mapper.MenuMapper;
import com.cjym.videoweb.mapper.RoleMapper;
import com.cjym.videoweb.mapper.RoleMenuBindMapper;
import com.cjym.videoweb.model.dto.ClientUserUpdateDto;
import com.cjym.videoweb.model.dto.MenuDto;
import com.cjym.videoweb.model.dto.UserDto;
import com.cjym.videoweb.model.dto.UserPageDto;
import com.cjym.videoweb.model.entity.Follow;
import com.cjym.videoweb.model.entity.Menu;
import com.cjym.videoweb.model.entity.Role;
import com.cjym.videoweb.model.entity.RoleMenuBind;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.model.vo.ClientUserVo;
import com.cjym.videoweb.model.vo.UpUserVo;
import com.cjym.videoweb.model.vo.UserAddVo;
import com.cjym.videoweb.model.vo.UserRegisterVo;
import com.cjym.videoweb.model.vo.UserEpisodeStatsVo;
import com.cjym.videoweb.model.vo.UserUpdateVo;
import com.cjym.videoweb.model.vo.CaptchaVo;
import com.cjym.videoweb.model.vo.PasswordVo;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.model.vo.UserStatsVo;
import com.cjym.videoweb.model.vo.UserVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.EmailSendService;
import com.cjym.videoweb.service.EsSearchService;
import com.cjym.videoweb.service.TokenExpiryConfigService;
import com.cjym.videoweb.service.UserInfoService;
import com.cjym.videoweb.service.WebConfigService;
import com.cjym.videoweb.utils.JWTUtil;
import com.cjym.videoweb.utils.MD5Util;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.github.eternalstone.captcha.gp.base.TextEntry;
import io.github.eternalstone.captcha.gp.captcha.SpecCaptcha;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private EmailSendService emailSendService;

    @Resource
    private TokenExpiryConfigService tokenExpiryConfigService;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuBindMapper roleMenuBindMapper;

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private WebConfigService webConfigService;

    /**
     * 聚合搜索门面：按开关在 ES 与数据库适配器间路由，ES 异常自动降级数据库
     */
    @Resource
    private SearchServiceFacade searchServiceFacade;

    /**
     * ES 索引同步服务：仅 elasticsearch.enabled=true 时存在，未启用时为 null（埋点静默跳过）
     */
    @Resource
    private ObjectProvider<EsSearchService> esSearchServiceProvider;

    @Value("${spring.mail.username}")
    private static String fromEmail;


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
        if (StringUtils.isEmpty(email) || !matcher.matches()) {
            throw new GlobeException(ResultEnum.EMAIL_ERROR.getMessage());
        }
        // 注册验证码：邮箱必须未被前台用户占用；登录验证码：邮箱必须已注册
        if (CaptchaConstant.REGISTER_EMAIL_CAPTCHA.equals(captchaType)) {
            UserDto frontUser = baseMapper.selectFrontDtoByEmail(email);
            if (!ObjectUtils.isEmpty(frontUser)) {
                throw new GlobeException("邮箱已被注册");
            }
            emailSendService.send(fromEmail, email, fromEmail, "注册验证码", "【cjym】本次的注册验证码为：" + code + "\n在5分钟内有效,如非本人操作,请忽略此条邮件");
            stringRedisTemplate.opsForValue().set(CaptchaConstant.REGISTER_EMAIL_CAPTCHA + captchaKey, code.toLowerCase(), 5, TimeUnit.MINUTES);
            return R.success(CaptchaVo.builder().key(captchaKey).build());
        }
        // 邮箱格式正确，发送验证码
        UserDto userDto = baseMapper.selectDtoByEmail(email);
        if (ObjectUtils.isEmpty(userDto)) {
            throw new GlobeException(ResultEnum.EMAIL_ERROR.getMessage());
        }
        emailSendService.send(fromEmail, email, fromEmail, "验证码", "【cjym】本次的验证码为：" + code + "\n在15分钟内有效,如非本人操作,请忽略此条邮件");
        stringRedisTemplate.opsForValue().set(CaptchaConstant.ADMIN_EMAIL_CAPTCHA + captchaKey, code.toLowerCase(), 5, TimeUnit.MINUTES);
        return R.success(CaptchaVo.builder().key(captchaKey).build());
    }

    /**
     * 用户登录
     * @param userLoginVo 登录信息
     * */
    @LogLogin
    @Override
    public R<Map<String,Object>> login(UserLoginVo userLoginVo) {
        UserDto user = null;
        String captChaKey;
        // 根据 loginSystem 决定服务器类型：0/前台 → SYS_FRONT，null或1/后台 → SYS_ADMIN
        String serverType = resolveServerType(userLoginVo.getLoginSystem());
        boolean isFront = BaseConstant.SYS_FRONT.equals(serverType);

        if(CaptchaConstant.ADMIN_IMG_CAPTCHA.equals(userLoginVo.getLoginType())){
            captChaKey= CaptchaConstant.ADMIN_IMG_CAPTCHA +userLoginVo.getKey();
            String redisCaptcha = stringRedisTemplate.opsForValue().getAndDelete(captChaKey);
            if(StringUtils.isEmpty(redisCaptcha)||!redisCaptcha.equalsIgnoreCase(userLoginVo.getCaptcha().toLowerCase())||StringUtils.isEmpty(userLoginVo.getCaptcha())){
                throw new GlobeException(ResultEnum.CAPTCHA_EXPIRE.getMessage());
            }
            // 前后台统一走 Spring Security 认证链，通过 details 传递 serverType 区分用户类型
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userLoginVo.getUsername(),
                    userLoginVo.getPassword().toLowerCase()
            );
            authToken.setDetails(serverType);
            Authentication authentication = authenticationManager.authenticate(authToken);
            user = (UserDto) authentication.getPrincipal();
        }else{
            captChaKey= CaptchaConstant.ADMIN_EMAIL_CAPTCHA +userLoginVo.getKey();
            String redisCaptcha = stringRedisTemplate.opsForValue().getAndDelete(captChaKey);
            if(StringUtils.isEmpty(redisCaptcha)||!redisCaptcha.equalsIgnoreCase(userLoginVo.getCaptcha().toLowerCase())||StringUtils.isEmpty(userLoginVo.getCaptcha())){
                throw new GlobeException(ResultEnum.CAPTCHA_EXPIRE.getMessage());
            }
            // 前台查 type=0 用户，后台查 type=1 用户
            user = isFront ? baseMapper.selectFrontDtoByEmail(userLoginVo.getEmail())
                    : baseMapper.selectDtoByEmail(userLoginVo.getEmail());
            if (user == null) {
                throw new GlobeException(ResultEnum.EMAIL_ERROR.getMessage());
            }
            if (!user.isEnabled()) {
                throw new GlobeException(ResultEnum.USER_DISABLE.getMessage());
            }
        }
        String accessToken = JWTUtil.createAccessToken(user.getId(), serverType,
                user.getUsername(), user.getAuthorities(),
                tokenExpiryConfigService.getAccessTokenExpiry(serverType));
        String refreshToken = JWTUtil.createRefreshToken(user.getId(), serverType,
                user.getUsername(),
                tokenExpiryConfigService.getRefreshTokenExpiry(serverType));
        saveRefreshToken(refreshToken,user.getId());
        return R.success(Map.of("accessToken",accessToken,"refreshToken",refreshToken));
    }

    /**
     * 前台用户注册（邮箱验证码 + 用户名密码，注册用户类型固定为前台普通用户，成功后自动登录签发token）
     * @param userRegisterVo 注册信息
     * */
    @Override
    public R<Map<String,Object>> register(UserRegisterVo userRegisterVo) {
        if (StringUtils.isEmpty(userRegisterVo.getUserName())) {
            throw new GlobeException("用户名不能为空");
        }
        if (StringUtils.isEmpty(userRegisterVo.getPassword())) {
            throw new GlobeException("密码不能为空");
        }
        if (StringUtils.isEmpty(userRegisterVo.getEmail())) {
            throw new GlobeException(ResultEnum.EMAIL_NULL_ERROR.getMessage());
        }
        if (StringUtils.isEmpty(userRegisterVo.getKey()) || StringUtils.isEmpty(userRegisterVo.getCaptcha())) {
            throw new GlobeException(ResultEnum.CODE_NULL_ERROR.getMessage());
        }
        // 校验注册邮箱验证码（一次性，校验后删除）
        String captchaKey = CaptchaConstant.REGISTER_EMAIL_CAPTCHA + userRegisterVo.getKey();
        String redisCaptcha = stringRedisTemplate.opsForValue().getAndDelete(captchaKey);
        if (StringUtils.isEmpty(redisCaptcha) || !redisCaptcha.equalsIgnoreCase(userRegisterVo.getCaptcha().toLowerCase())) {
            throw new GlobeException(ResultEnum.CAPTCHA_EXPIRE.getMessage());
        }
        // 校验用户名/邮箱在后台普通用户（type=0）范围内未被占用
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getType, BaseConstant.USER_TYPE_USER);
        wrapper.and(w -> w.eq(UserInfo::getUserName, userRegisterVo.getUserName())
                .or().eq(UserInfo::getEmail, userRegisterVo.getEmail()));
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new GlobeException("用户名或邮箱已被注册");
        }
        // 构建用户实体
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(userRegisterVo.getUserName());
        userInfo.setEmail(userRegisterVo.getEmail());
        // 昵称未传时默认使用用户名
        userInfo.setNikeName(StringUtils.isEmpty(userRegisterVo.getNikeName())
                ? userRegisterVo.getUserName() : userRegisterVo.getNikeName());
        // 生成16位随机数字uid
        userInfo.setUid(generateUid());
        // 生成盐值并加密密码
        String salt = MD5Util.generateSalt();
        userInfo.setSalt(salt);
        userInfo.setPassword(MD5Util.md5WithSalt(userRegisterVo.getPassword(), salt));
        // 前台普通用户：状态正常、默认前台角色
        userInfo.setType(BaseConstant.USER_TYPE_USER);
        userInfo.setStatus(0);
        userInfo.setRoleId(BaseConstant.DEFAULT_FRONT_USER_ROLE_ID);
        baseMapper.insert(userInfo);
        // 同步 ES 索引
        refreshUserEs(userInfo.getId());
        // 注册成功后自动登录：按前台用户查询 UserDto（含角色权限）并签发 token
        UserDto userDto = baseMapper.selectFrontDtoById(userInfo.getId());
        String serverType = BaseConstant.SYS_FRONT;
        String accessToken = JWTUtil.createAccessToken(userDto.getId(), serverType,
                userDto.getUsername(), userDto.getAuthorities(),
                tokenExpiryConfigService.getAccessTokenExpiry(serverType));
        String refreshToken = JWTUtil.createRefreshToken(userDto.getId(), serverType,
                userDto.getUsername(),
                tokenExpiryConfigService.getRefreshTokenExpiry(serverType));
        saveRefreshToken(refreshToken, userDto.getId());
        return R.success(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    /**
     * 根据 loginSystem 解析 serverType
     */
    private String resolveServerType(Integer loginSystem) {
        if (loginSystem != null && loginSystem == 0) {
            return BaseConstant.SYS_ADMIN;
        }
        return BaseConstant.SYS_FRONT;
    }

    @Override
    public R<UserVo> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = JWTUtil.getUserId(token);
        UserInfo userInfo = baseMapper.selectById(userId);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userInfo,userVo);
        Long roleId = userInfo.getRoleId();
        Role role = roleMapper.selectById(roleId);
        userVo.setRoleName(role.getName());
        userVo.setRoleLevel(role.getLevel());
        LambdaQueryWrapper<RoleMenuBind> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RoleMenuBind::getRoleId, roleId);
        List<RoleMenuBind> roleMenuBinds = roleMenuBindMapper.selectList(lambdaQueryWrapper);
        if(roleMenuBinds!=null && !roleMenuBinds.isEmpty()){
            Set<Long> menuIds = roleMenuBinds.stream().map(RoleMenuBind::getMenuId).collect(Collectors.toSet());
            LambdaQueryWrapper<Menu> menuLambdaQueryWrapper=new LambdaQueryWrapper<>();
            menuLambdaQueryWrapper.in(Menu::getId, menuIds);
            menuLambdaQueryWrapper.eq(Menu::getStatus, true);
            menuLambdaQueryWrapper.orderByAsc(Menu::getDescSort, Menu::getId);
            List<Menu> menus = menuMapper.selectList(menuLambdaQueryWrapper);
            // 转换为 MenuDto 列表
            List<MenuDto> allDtos = menus.stream().map(m -> {
                MenuDto dto = new MenuDto();
                dto.setId(m.getId());
                dto.setMenuName(m.getMenuName());
                dto.setPath(m.getPath());
                dto.setMenuIdentification(m.getMenuIdentification());
                dto.setStatus(m.getStatus());
                dto.setParentId(m.getParentId());
                dto.setDescSort(m.getDescSort());
                dto.setIcon(m.getIcon());
                dto.setCategory(m.getCategory());
                return dto;
            }).toList();
            // 建立 id → MenuDto 映射
            Map<Long, MenuDto> idMap = allDtos.stream()
                    .collect(Collectors.toMap(MenuDto::getId, d -> d));
            // 将子节点挂载到父节点的 children 中
            for (MenuDto dto : allDtos) {
                if (dto.getParentId() != null && dto.getParentId() != 0) {
                    MenuDto parent = idMap.get(dto.getParentId());
                    if (parent != null) {
                        dto.setParentMenuName(parent.getMenuName());
                        parent.getChildren().add(dto);
                    }
                }
            }
            // 过滤出根节点作为菜单树
            List<MenuDto> menuTree = allDtos.stream()
                    .filter(d -> d.getParentId() == null || d.getParentId() == 0).sorted(Comparator.comparing(MenuDto::getDescSort))
                    .collect(Collectors.toList());
            userVo.setMenuList(menuTree);
        }

        return R.success(userVo);
    }

    @Override
    public R<ClientUserVo> getClientUserInfo(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if(token==null){
            throw new GlobeException(ResultEnum.TOKEN_ERROR.getMessage());
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = JWTUtil.getUserId(token);
        UserInfo userInfo = baseMapper.selectById(userId);
        if (userInfo == null) {
            throw new GlobeException("用户不存在");
        }
        ClientUserVo vo = new ClientUserVo();
        BeanUtils.copyProperties(userInfo, vo);
        // 大会员到期时间：仅当前有效时返回，非大会员或已过期时置空（与大会员状态接口口径一致）
        if (Boolean.FALSE.equals(userInfo.getIsVip()) || userInfo.getVipExpireTime() == null
                || !userInfo.getVipExpireTime().isAfter(LocalDateTime.now())) {
            vo.setVipExpireTime(null);
        }
        // 统计关注数与粉丝数（@TableLogic 自动过滤已取关记录）
        Long followCount = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userInfo.getId()));
        Long fansCount = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowUserId, userInfo.getId()));
        vo.setFollowCount(followCount);
        vo.setFansCount(fansCount);
        // 统计所有剧集的累计获赞数与播放数（无投稿时为0）
        UserEpisodeStatsVo stats = videoMapper.selectUserEpisodeStats(userInfo.getId());
        vo.setTotalLikeCount(stats == null ? 0L : stats.getTotalLikeCount());
        vo.setTotalPlayCount(stats == null ? 0L : stats.getTotalPlayCount());
        return R.success(vo);
    }

    @Override
    public R<String> updateClientUserInfo(HttpServletRequest request, ClientUserUpdateDto dto) {
        String token = request.getHeader("authorization");
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = JWTUtil.getUserId(token);
        UserInfo userInfo = baseMapper.selectById(userId);
        if (userInfo == null) {
            return R.error("用户不存在");
        }
        if (dto.getNikeName() == null && dto.getAvatar() == null && dto.getSex() == null
                && dto.getBirthday() == null && dto.getDescript() == null) {
            return R.error("至少需要修改一个字段");
        }
        userInfo.setNikeName(dto.getNikeName());
        userInfo.setAvatar(dto.getAvatar());
        userInfo.setSex(dto.getSex());
        userInfo.setBirthday(dto.getBirthday());
        userInfo.setDescript(dto.getDescript());
        baseMapper.updateById(userInfo);
        // 同步 ES 索引（昵称/头像/签名变更影响UP主搜索展示）
        refreshUserEs(userId);
        return R.success("修改成功");
    }

    @Override
    public R<Map<String, Object>> refresh(HttpServletRequest request) {
        String requestRefreshToken = request.getHeader("refresh-token");
        // 1. 验证 Refresh Token 是否有效
        if (!verifyRefreshToken(requestRefreshToken)) {
            return R.error(ResultEnum.FORBIDDEN_ERROR);
        }
        // 2. 从 JWT 中获取用户ID和 serverType
        Long userId = JWTUtil.getUserId(requestRefreshToken);
        String serverType = JWTUtil.getServerType(requestRefreshToken);
        // 3. 撤销旧 Refresh Token（Rotation：防止旧 token 继续使用）
        deleteByToken(requestRefreshToken);
        // 4. 生成新的 Access Token 和 Refresh Token
        boolean isFront = BaseConstant.SYS_FRONT.equals(serverType);
        UserDto userDetails = isFront ? baseMapper.selectFrontDtoById(userId)
                : baseMapper.selectDtoById(userId);
        String newAccessToken = JWTUtil.createAccessToken(userDetails.getId(), serverType,
                userDetails.getUsername(), userDetails.getAuthorities(),
                tokenExpiryConfigService.getAccessTokenExpiry(serverType));
        String newRefreshToken = JWTUtil.createRefreshToken(userDetails.getId(), serverType,
                userDetails.getUsername(),
                tokenExpiryConfigService.getRefreshTokenExpiry(serverType));
        saveRefreshToken(newRefreshToken,userId);
        // 5. 返回新的 Token 对
        return R.success(Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));
    }

    @Override
    public R<String> logout(HttpServletRequest request) {
        String refreshToken = request.getHeader("refresh-token");
        if (StringUtils.isNotEmpty(refreshToken)
                && JWTUtil.validateToken(refreshToken)
                && BaseConstant.TOKEN_REFRESH.equals(JWTUtil.getTokenType(refreshToken))) {
            deleteByToken(refreshToken);
        }
        return R.success("登出成功");
    }

    @Override
    public R<String> resetPassword(PasswordVo passwordVo) {
        Long userId = passwordVo.getUserId();
        Integer type=passwordVo.getType();
        if(userId==null||type==null){
            throw new GlobeException("参数错误，请检查用户ID和类型");
        }
        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserInfo::getId, userId);
        lambdaQueryWrapper.eq(UserInfo::getType, type);
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

    @Override
    public R<String> addUser(UserAddVo userAddVo) {
        if (StringUtils.isEmpty(userAddVo.getUserName())) {
            return R.error("用户名不能为空");
        }
        if (StringUtils.isEmpty(userAddVo.getPassword())) {
            return R.error("密码不能为空");
        }
        // 检查用户名是否已存在
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserName, userAddVo.getUserName());
        wrapper.eq(UserInfo::getType,userAddVo.getType());
        if (baseMapper.selectCount(wrapper) > 0) {
            return R.error("用户名已存在");
        }
        // 构建用户实体
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userAddVo, userInfo);
        // 生成16位随机数字uid
        userInfo.setUid(generateUid());
        // 生成盐值并加密密码
        String salt = MD5Util.generateSalt();
        String encryptedPassword = MD5Util.md5WithSalt(userAddVo.getPassword(), salt);
        userInfo.setSalt(salt);
        userInfo.setPassword(encryptedPassword);
        // 默认状态为正常
        if (userInfo.getStatus() == null) {
            userInfo.setStatus(0);
        }
        if(Objects.equals(userInfo.getType(), BaseConstant.USER_TYPE_ADMIN)) {
            // 从web_config配置（Redis缓存优先）读取默认角色ID，未配置时回退常量默认值
            String defaultRoleValue = webConfigService.getConfigValue(
                    BaseConstant.WEB_CONFIG_CATEGORY_DEFAULT, BaseConstant.WEB_CONFIG_KEY_DEFAULT_ROLE);
            userInfo.setRoleId(StringUtils.isNotEmpty(defaultRoleValue)
                    ? Long.valueOf(defaultRoleValue) : BaseConstant.DEFAULT_FRONT_USER_ROLE_ID);
        }
        baseMapper.insert(userInfo);
        // 同步 ES 索引
        refreshUserEs(userInfo.getId());
        return R.success("新增用户成功");
    }

    @Override
    public R<String> updateUser(UserUpdateVo userUpdateVo) {
        if (userUpdateVo.getId() == null) {
            return R.error("用户ID不能为空");
        }
        UserInfo existUser = baseMapper.selectById(userUpdateVo.getId());
        if (existUser == null) {
            return R.error("用户不存在");
        }
        // 仅更新传入的非空字段
        LambdaUpdateWrapper<UserInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserInfo::getId, userUpdateVo.getId());
        if (userUpdateVo.getEmail() != null) {
            wrapper.set(UserInfo::getEmail, userUpdateVo.getEmail());
        }
        if (userUpdateVo.getNikeName() != null) {
            wrapper.set(UserInfo::getNikeName, userUpdateVo.getNikeName());
        }
        if (userUpdateVo.getAvatar() != null) {
            wrapper.set(UserInfo::getAvatar, userUpdateVo.getAvatar());
        }
        if (userUpdateVo.getType() != null) {
            wrapper.set(UserInfo::getType, userUpdateVo.getType());
        }
        if (userUpdateVo.getRoleId() != null) {
            wrapper.set(UserInfo::getRoleId, userUpdateVo.getRoleId());
        }
        if (userUpdateVo.getIsVip() != null) {
            wrapper.set(UserInfo::getIsVip, userUpdateVo.getIsVip());
        }
        if (userUpdateVo.getStatus() != null) {
            wrapper.set(UserInfo::getStatus, userUpdateVo.getStatus());
        }
        if (userUpdateVo.getSex() != null) {
            wrapper.set(UserInfo::getSex, userUpdateVo.getSex());
        }
        if (userUpdateVo.getBirthday() != null) {
            wrapper.set(UserInfo::getBirthday, userUpdateVo.getBirthday());
        }
        if (userUpdateVo.getDescript() != null) {
            wrapper.set(UserInfo::getDescript, userUpdateVo.getDescript());
        }
        baseMapper.update(null, wrapper);
        // 同步 ES 索引
        refreshUserEs(userUpdateVo.getId());
        return R.success("更新用户成功");
    }

    @Override
    public R<String> deleteUser(Long id) {
        if (id == null) {
            return R.error("用户ID不能为空");
        }
        UserInfo existUser = baseMapper.selectById(id);
        if (existUser == null) {
            return R.error("用户不存在");
        }
        // 逻辑删除（@TableLogic 自动处理）
        baseMapper.deleteById(id);
        // 同步 ES 索引（refreshUser 查不到用户时会删除文档）
        refreshUserEs(id);
        return R.success("删除用户成功");
    }

    @Override
    public R<IPage<UserPageDto>> listUsers(Integer page, Integer size,
                                           String userName, String nikeName, String email,
                                           Integer status, Long roleId, Integer type, Boolean isVip) {
        Page<UserPageDto> pageParam = new Page<>(page, size);
        IPage<UserPageDto> result = baseMapper.selectUserPage(pageParam, userName, nikeName, email, status, roleId, type, isVip);
        return R.success(result);
    }

    @Override
    public UserStatsVo getUserStats() {
        UserStatsVo vo = baseMapper.selectUserStats();
        // 计算较昨日的变化差值
        Long ydNew = vo.getYesterdayNewCount() != null ? vo.getYesterdayNewCount() : 0L;
        Long ydActive = vo.getYesterdayActiveCount() != null ? vo.getYesterdayActiveCount() : 0L;
        Long tdNew = vo.getTodayNewCount() != null ? vo.getTodayNewCount() : 0L;
        Long tdActive = vo.getTodayActiveCount() != null ? vo.getTodayActiveCount() : 0L;
        vo.setTodayNewDiff(tdNew - ydNew);
        vo.setTodayActiveDiff(tdActive - ydActive);
        // 清理内部字段，不返回给前端
        vo.setYesterdayNewCount(null);
        vo.setYesterdayActiveCount(null);
        return vo;
    }

    @Override
    public IPage<UpUserVo> pageSearchUp(String keyword, Integer page, Integer size, String sortType) {
        // 搜索实现（ES/数据库适配器）由 SearchServiceFacade 按开关路由，ES 异常自动降级数据库
        IPage<UpUserVo> result = searchServiceFacade.searchUp(keyword, page, size, sortType);
        // 关注数/粉丝数由 ES 文档返回（事件机制同步维护）或数据库查询自带，这里仅填充当前用户是否已关注
        fillFollowStatus(result.getRecords());
        return result;
    }

    @Override
    public List<UserPageDto> globalSearch(String keyword, int limit) {
        return baseMapper.selectGlobalSearch(keyword, limit);
    }

    @Override
    public UpUserVo getUpUserInfo(String userUid) {
        if (userUid == null || userUid.isBlank()) {
            throw new GlobeException("UP主uid不能为空");
        }
        UserInfo user = baseMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUid, userUid.trim()));
        if (user == null) {
            throw new GlobeException("UP主不存在");
        }
        UpUserVo vo = new UpUserVo();
        vo.setUid(user.getUid());
        vo.setNikeName(user.getNikeName());
        vo.setAvatar(user.getAvatar());
        vo.setDescript(user.getDescript());
        vo.setSex(user.getSex());
        vo.setIsVip(user.getIsVip());
        // 关注数/粉丝数（@TableLogic 自动过滤已取关记录）
        vo.setFollowCount(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, user.getId())));
        vo.setFansCount(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowUserId, user.getId())));
        // 累计获赞/累计播放（无投稿时为0）
        UserEpisodeStatsVo stats = videoMapper.selectUserEpisodeStats(user.getId());
        vo.setTotalLikeCount(stats == null ? 0L : stats.getTotalLikeCount());
        vo.setTotalPlayCount(stats == null ? 0L : stats.getTotalPlayCount());
        // 当前用户是否已关注（游客为 false）
        Long currentUserId = getCurrentUserId();
        vo.setIsFollowed(currentUserId != null && followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, user.getId())) > 0);
        return vo;
    }

    @Override
    public R<String> reset(PasswordVo passwordVo) {
        Long userId = passwordVo.getUserId();
        Integer type = passwordVo.getType();
        if(userId == null || type == null){
            return R.error("参数错误，请检查用户ID和类型");
        }
        LambdaQueryWrapper<UserInfo> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserInfo::getId, userId);
        lambdaQueryWrapper.eq(UserInfo::getType,type);
        UserInfo userInfo = baseMapper.selectOne(lambdaQueryWrapper);
        if(ObjectUtils.isEmpty(userInfo)){
            throw new GlobeException("用户不存在");
        }
        String salt = MD5Util.generateSalt();
        // 默认密码优先从 Redis 读取，未命中回源数据库查询（web_config 表 security 分类）
        String defaultPassword = webConfigService.getConfigValue(
                BaseConstant.WEB_CONFIG_CATEGORY_SECURITY, BaseConstant.WEB_CONFIG_KEY_DEFAULT_PASSWORD);
        // 配置缺失时兜底使用默认密码，保证重置功能可用
        if (StringUtils.isEmpty(defaultPassword)) {
            defaultPassword = "123456";
        }
        String newPassword = MD5Util.md5WithSalt(defaultPassword, salt);
        userInfo.setSalt(salt);
        userInfo.setPassword(newPassword);
        baseMapper.updateById(userInfo);
        return R.success("密码重置成功");
    }

    /**
     * 为UP主搜索记录填充当前用户是否已关注的标记（未登录时全部为 false）；
     * 先按 uid 批量反查用户主键id，再一次查询当前用户的关注记录，避免逐条查询
     */
    private void fillFollowStatus(List<UpUserVo> vos) {
        if (vos == null || vos.isEmpty()) {
            return;
        }
        vos.forEach(vo -> vo.setIsFollowed(false));
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return;
        }
        // uid → 用户主键id（follow 表按主键关联）
        List<String> uids = vos.stream().map(UpUserVo::getUid)
                .filter(uid -> uid != null && !uid.isBlank())
                .toList();
        if (uids.isEmpty()) {
            return;
        }
        Map<String, Long> uidIdMap = baseMapper.selectList(new LambdaQueryWrapper<UserInfo>()
                        .select(UserInfo::getUid, UserInfo::getId)
                        .in(UserInfo::getUid, uids)).stream()
                .collect(Collectors.toMap(UserInfo::getUid, UserInfo::getId, (a, b) -> a));
        Set<Long> followedIds = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getUserId, currentUserId)
                        .in(Follow::getFollowUserId, uidIdMap.values())).stream()
                .map(Follow::getFollowUserId)
                .collect(Collectors.toSet());
        vos.forEach(vo -> {
            Long userId = uidIdMap.get(vo.getUid());
            vo.setIsFollowed(userId != null && followedIds.contains(userId));
        });
    }

    /**
     * 从请求头解析当前登录用户id（未携带合法 token 时返回 null）
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("authorization");
            if (StringUtils.isEmpty(token)) {
                return null;
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return JWTUtil.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 用户变更后同步 ES 索引（ES 未装配或同步失败时静默忽略，不影响主流程）
     */
    private void refreshUserEs(Long userId) {
        EsSearchService esSearchService = esSearchServiceProvider.getIfAvailable();
        if (esSearchService == null) {
            return;
        }
        try {
            esSearchService.refreshUser(userId);
        } catch (Exception e) {
            log.warn("同步用户到 ES 失败 userId={}: {}", userId, e.getMessage());
        }
    }

    public void saveRefreshToken(String refreshToken,Long userId) {
        // 计算 token 的剩余有效期（毫秒）
        long expiration = JWTUtil.parseClaims(refreshToken).getExpiration().getTime() - System.currentTimeMillis();
        String userZSetKey = BaseConstant.USER_TOKENS_ZSET_PREFIX + userId;
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
        stringRedisTemplate.opsForValue().set(BaseConstant.REFRESH_TOKEN_PREFIX + refreshToken, userId.toString(), expiration, TimeUnit.MILLISECONDS);
        // 5. 将新 Token 加入用户 ZSet，score 为当前时间戳
        stringRedisTemplate.opsForZSet().add(userZSetKey, refreshToken, System.currentTimeMillis());
        // 这里设置一个较长的时间，保证不会无限增长
        stringRedisTemplate.expire(userZSetKey, expiration + 60000, TimeUnit.MILLISECONDS);
    }

    private void cleanExpiredTokens(String userZSetKey) {
        long now = System.currentTimeMillis();
        long minScore = now - TokenExpiryConfigService.MAX_REFRESH_WINDOW;
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

    /**
     * 生成16位随机数字uid
     */
    private String generateUid() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }
}

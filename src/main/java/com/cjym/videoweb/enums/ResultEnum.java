package com.cjym.videoweb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum ResultEnum {
  SUCCESS(200,"数据返回啦 《《"),
  EMAIL_SEND(200,"邮件发送成功"),
  PASSWORD_OR_USERNAME_ERROR(401,"用户名或密码错误"),
  USER_ERROR(402,"用户已被禁用"),
  ERROR(500,"响应失败"),
  UP_LOAD_ERROR(500,"文件上传失败"),
  MAINTAIN(404,"服务器正在维护"),
  ROLE_LEVE_ERROR(451, "无法操作自身权限更高的角色"),
  MOBILE_NULL_ERROR(-202, "手机号码不能为空"),
  MOBILE_ERROR(-203, "手机号码不正确"),
  PASSWORD_NULL_ERROR(204, "密码不能为空"),
  CODE_NULL_ERROR(205, "验证码不能为空"),
  CODE_ERROR(206, "验证码错误"),
  MOBILE_EXIST_ERROR(207, "手机号已被注册"),
  LOGIN_MOBILE_ERROR(208, "用户不存在"),
  LOGIN_PASSWORD_ERROR(209, "密码错误"),
  LOGIN_LOKED_ERROR(210, "用户被锁定"),
  LOGIN_AUTH_ERROR(-211, "未登录"),
  USER_BIND_IDCARD_EXIST_ERROR(-301, "身份证号码已绑定"),
  USER_NO_BIND_ERROR(302, "用户未绑定"),
  USER_NO_AMOUNT_ERROR(303, "用户信息未审核"),
  USER_AMOUNT_LESS_ERROR(304, "您的借款额度不足"),
  LEND_INVEST_ERROR(305, "当前状态无法投标"),
  LEND_FULL_SCALE_ERROR(306, "已满标，无法投标"),
  NOT_SUFFICIENT_FUNDS_ERROR(307, "余额不足，请充值"),
  FORBIDDEN_ERROR(403, "无权限访问"),
  PAY_UNIFIEDORDER_ERROR(401, "统一下单错误"),
  ALIYUN_SMS_LIMIT_CONTROL_ERROR(-502, "短信发送过于频繁"),//业务限流
  ALIYUN_SMS_ERROR(-503, "短信发送失败"),//其他失败
  WEIXIN_CALLBACK_PARAM_ERROR(-601, "回调参数不正确"),
  WEIXIN_FETCH_ACCESSTOKEN_ERROR(-602, "获取access_token失败"),
  WEIXIN_FETCH_USERINFO_ERROR(-603, "获取用户信息失败"),
  EMAIL_NULL_ERROR(207, "邮箱不能为空"),
  EMAIL_ERROR(208, "邮箱不存在"), TOKEN_ERROR(503, "token错误"),
  EMAIL_SEND_ERROR(408, "邮件发送失败"),
  AUTH_PERMISSION_NULL(410,"暂无任何权限，请联系管理员"),
  CAPTCHA_EXPIRE(500, "验证码已过期"),
  USER_DISABLE(411, "用户已禁用");


  //响应状态码
  private final Integer code;

  //描述信息
  private final String message;



}

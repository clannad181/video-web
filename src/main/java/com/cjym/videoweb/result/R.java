package com.cjym.videoweb.result;


import com.cjym.videoweb.enums.ResultEnum;
import lombok.Data;

@Data
public class R<T> {

  private Integer code;

  private String message;

  private T data;

  private R(){}

  //响应成功（无返回结果）
  public static<T> R<T> success(){
    R<T> r = new R<>();
    r.setCode(ResultEnum.SUCCESS.getCode());
    r.setMessage(ResultEnum.SUCCESS.getMessage());
    return r;
  }

  public static<T> R<T> success(Boolean flag){
    R<Boolean> r = new R<>();
    r.setCode(ResultEnum.SUCCESS.getCode());
    r.setMessage(ResultEnum.SUCCESS.getMessage());
    r.setData(flag);
    return (R<T>) r;
  }

  //相应成功（有返回结果）
  public static<T> R<T> success(T obj){
    R<T> r = new R<>();
    r.setCode(ResultEnum.SUCCESS.getCode());
    r.setMessage(ResultEnum.SUCCESS.getMessage());
    r.setData(obj);
    return r;
  }

  //响应失败
  public static<T> R<T> error(){
    R<T> r = new R<T>();
    r.setCode(ResultEnum.ERROR.getCode());
    r.setMessage(ResultEnum.ERROR.getMessage());
    return r;
  }

  public static<T> R<T> error(Integer code){
    R<T> r = new R<T>();
    r.setCode(code);
    r.setMessage(ResultEnum.ERROR.getMessage());
    return r;
  }

  //自定义响应信息
  public static<T> R<T> success(ResultEnum rs){
    R<T> r = new R<T>();
    r.setCode(rs.getCode());
    r.setMessage(rs.getMessage());
    return r;
  }

  //设置特定的消息
  public static<T> R<T> success(String message){
    R<T> r = new R<T>();
    r.setCode(ResultEnum.SUCCESS.getCode());
    r.setMessage(message);
    return r;
  }

  //设置特定的消息和数据
  public static<T> R<T> success(String message,T obj){
    R<T> r = new R<T>();
    r.setCode(ResultEnum.SUCCESS.getCode());
    r.setMessage(message);
    r.setData(obj);
    return r;
  }

  public static<T> R<T> success(ResultEnum resultEnum,T obj){
    R<T> r = new R<T>();
    r.setCode(ResultEnum.SUCCESS.getCode());
    r.setMessage(resultEnum.getMessage());
    r.setData(obj);
    return r;
  }

  //自定义失败消息
  public static<T> R<T> error(String message){
    R<T> r = new R<T>();
    r.setCode(ResultEnum.ERROR.getCode());
    r.setMessage(message);
    return r;
  }


  public static<T> R<T> error(ResultEnum e){
    R<T> r = new R<T>();
    r.setCode(e.getCode());
    r.setMessage(e.getMessage());
    return r;
  }
}

package cn.danxing.platform;
public class ApiException extends RuntimeException {
    public final int status;
    public ApiException(int status,String message){super(message);this.status=status;}
    public static ApiException bad(String s){return new ApiException(400,s);}
    public static ApiException missing(){return new ApiException(404,"记录不存在或无权访问");}
    public static ApiException unauthorized(){return new ApiException(401,"请先登录");}
}

package cn.danxing.platform;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import java.util.Map;

@RestControllerAdvice
public class ApiErrors {
    private static final Logger LOG=LoggerFactory.getLogger(ApiErrors.class);
    @ExceptionHandler(ApiException.class) ResponseEntity<?> app(ApiException e){return ResponseEntity.status(e.status).body(Map.of("message",e.getMessage()));}
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("message",e.getBindingResult().getFieldErrors().get(0).getDefaultMessage()));}
    @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<?> json(){return ResponseEntity.badRequest().body(Map.of("message","请求格式或日期无效"));}
    @ExceptionHandler(MaxUploadSizeExceededException.class) ResponseEntity<?> size(){return ResponseEntity.status(413).body(Map.of("message","附件不能超过 20 MB"));}
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict(){return ResponseEntity.status(409).body(Map.of("message","数据冲突，请检查重复账号或关联记录"));}
    @ExceptionHandler(AccessDeniedException.class) ResponseEntity<?> denied(){return ResponseEntity.status(403).body(Map.of("message","权限不足"));}
    @ExceptionHandler(Exception.class) ResponseEntity<?> other(Exception e){LOG.error("Request failed",e);return ResponseEntity.status(500).body(Map.of("message","服务暂时不可用，请稍后重试"));}
}

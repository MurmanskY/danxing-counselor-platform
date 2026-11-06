package cn.danxing.platform;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
@Configuration
public class StorageConfig {
    @Bean MinioClient minio(@Value("${app.minio.endpoint}") String endpoint,@Value("${app.minio.access-key}") String key,@Value("${app.minio.secret-key}") String secret){return MinioClient.builder().endpoint(endpoint).credentials(key,secret).build();}
}

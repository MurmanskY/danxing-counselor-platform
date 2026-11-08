package cn.danxing.platform;
import org.slf4j.*;
import org.springframework.cache.*;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.*;
@Configuration
public class CacheConfig {
    private static final Logger log=LoggerFactory.getLogger(CacheConfig.class);
    @Bean RedisCacheWriter redisCacheWriter(RedisConnectionFactory connectionFactory){
        return RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);
    }
    @Bean CacheErrorHandler cacheErrorHandler(){
        return new SimpleCacheErrorHandler(){
            @Override public void handleCacheGetError(RuntimeException exception,Cache cache,Object key){
                log.warn("缓存读取失败，回源查询（cache={}, key={}）：{}",cache.getName(),key,exception.toString());
            }
            @Override public void handleCachePutError(RuntimeException exception,Cache cache,Object key,Object value){
                log.warn("缓存写入失败，已忽略（cache={}, key={}）：{}",cache.getName(),key,exception.toString());
            }
            @Override public void handleCacheEvictError(RuntimeException exception,Cache cache,Object key){
                log.warn("缓存清理失败，已忽略（cache={}, key={}）：{}",cache.getName(),key,exception.toString());
            }
            @Override public void handleCacheClearError(RuntimeException exception,Cache cache){
                log.warn("缓存清空失败，已忽略（cache={}）：{}",cache.getName(),exception.toString());
            }
        };
    }
}

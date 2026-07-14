package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis optionnel pour le cache. Désactiver localement avec {@code app.redis.enabled=false}
 * si aucun serveur Redis n'écoute (évite l'erreur DNS / connexion à l'enregistrement patient).
 */
@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.timeout:800}")
    private int redisTimeoutMs;

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = false)
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setTestOnBorrow(false);
        poolConfig.setBlockWhenExhausted(false);
        poolConfig.setMaxWaitMillis(redisTimeoutMs);
        poolConfig.setJmxEnabled(false);

        log.info("JedisPool Redis configuré sur {}:{} (timeout {} ms)", redisHost, redisPort, redisTimeoutMs);
        return new JedisPool(poolConfig, redisHost, redisPort, redisTimeoutMs);
    }
}

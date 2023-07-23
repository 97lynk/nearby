package io.a97lynk.nearby.cache;

import io.a97lynk.nearby.dto.Location;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class LocationCache {

    private final RedisTemplate<String, Object> redisTemplate;

//    private static final String LOCATION_MAP = "LOCATION";

    private static final long HISTORY_EXPIRE_TIME = 30L; // 30s

    public void updateLocation(Long userId, Location location) {
        log.info("updateLocation {} {}", userId, location);
        redisTemplate.opsForValue().set(userId.toString(), location, HISTORY_EXPIRE_TIME, TimeUnit.SECONDS);
    }
}

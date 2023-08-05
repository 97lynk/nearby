package io.a97lynk.nearby.repository;

import io.a97lynk.nearby.ObjectUtil;
import io.a97lynk.nearby.dto.cache.LocationCacheDto;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LocationCacheRepository {

    private final static String KEY_TEMPLATE = "Location:%s";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveLocation(String userId, LocationCacheDto location) {
        location.setUserId(userId);
        redisTemplate.opsForValue().set(String.format(KEY_TEMPLATE, userId), ObjectUtil.object2Json(location), 6, TimeUnit.SECONDS);
    }

    public LocationCacheDto getLocation(String userId) {
        String value = (String) redisTemplate.opsForValue().get(String.format(KEY_TEMPLATE, userId));
        if (value == null) return null;
        return ObjectUtil.jsonToObject(value, LocationCacheDto.class);
    }

    public List<LocationCacheDto> getLocations(List<String> userIds) {
        List<String> keys = userIds.stream().map(u -> String.format(KEY_TEMPLATE, u)).collect(Collectors.toList());
        return redisTemplate.opsForValue().multiGet(keys)
                .stream()
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .map(v ->  ObjectUtil.jsonToObject(v, LocationCacheDto.class))
                .collect(Collectors.toList());
    }
}

package io.a97lynk.nearby.websocket.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LocationCacheDto implements Serializable {

    private String userId;

    private double longitude;

    private double latitude;

    private Date updatedDate;
}

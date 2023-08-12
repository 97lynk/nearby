package io.a97lynk.nearby.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Location implements Serializable {

    private static final long serialVersionUID = -7264709203117701718L;

    private String userId;

    private double longitude;

    private double latitude;

    private String source;

    private boolean nearby;

    private double distance;
}

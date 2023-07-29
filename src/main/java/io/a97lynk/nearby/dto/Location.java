package io.a97lynk.nearby.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location implements Serializable {

    private static final long serialVersionUID = -7264709203117701718L;

    private String userId;

    private double longitude;

    private double latitude;

    private boolean nearby;

    @Override
    public String toString() {
        return "Location{" +
                "userId='" + userId + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}

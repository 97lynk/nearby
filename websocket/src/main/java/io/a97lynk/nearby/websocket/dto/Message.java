package io.a97lynk.nearby.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String userId;
    private String from;
    private String text;
    private String time;

    // getters and setters
}

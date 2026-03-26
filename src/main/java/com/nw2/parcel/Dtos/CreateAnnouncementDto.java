package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreateAnnouncementDto {

    private String title;
    private String subtitle;
    private String content;
    private String coverImageUrl;
    private Integer categoryId;
    private Integer dormId;
    private Boolean pinned;
    private Boolean sendNotification;
    private Integer priority;
    private LocalDateTime publishAt;
    private Boolean publishNow;
}
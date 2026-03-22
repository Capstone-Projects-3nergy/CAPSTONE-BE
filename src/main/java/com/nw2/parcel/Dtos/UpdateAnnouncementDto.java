package com.nw2.parcel.Dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateAnnouncementDto {
    private String title;
    private String subtitle;
    private String content;
    private String coverImageUrl;
    private Integer categoryId;
    private Boolean pinned;
    private Integer priority;
    private Boolean sendNotification;
    private LocalDateTime publishAt;
    private Boolean publishNow;
}

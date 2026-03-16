package com.nw2.parcel.Dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDto {
    private Integer id;
    private String title;
    private String subtitle;
    private String content;
    private String coverImageUrl;
    private String category;
    private Boolean pinned;
    private Integer priority;
    private LocalDateTime publishAt;
    private Integer viewCount;
}

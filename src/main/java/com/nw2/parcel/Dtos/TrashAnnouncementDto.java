package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrashAnnouncementDto {

    private Integer announcementId;
    private String title;
    private String subtitle;
    private String categoryName;
    private LocalDateTime publishAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

}

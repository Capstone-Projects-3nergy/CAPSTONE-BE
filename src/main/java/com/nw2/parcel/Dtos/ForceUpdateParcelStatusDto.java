package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import lombok.Data;

@Data
public class ForceUpdateParcelStatusDto {
    private Parcels.Status status;  // สถานะใหม่ที่ admin อยากเซ็ต
    private String note;            // เหตุผล / คำอธิบาย (optional)
}

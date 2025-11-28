package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDormId implements Serializable {
    private Integer dormId;
    private Integer userId;
}


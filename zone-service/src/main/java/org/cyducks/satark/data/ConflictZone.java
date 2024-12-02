package org.cyducks.satark.data;

import jakarta.persistence.*;
import lombok.*;
import org.cyducks.satark.converter.GeoPointListConverter;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conflict_zones")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ConflictZone {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String description;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = GeoPointListConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    private List<GeoPoint> redZone;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = GeoPointListConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    private List<GeoPoint> orangeZone;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = GeoPointListConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    private List<GeoPoint> yellowZone;

    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedDate;

}

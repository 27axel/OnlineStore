package ru.skypro.homework.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "image_entity")
public class ImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String mediaType;
    @Lob
    private byte[] data;
    private String fileName;
    @OneToOne
    @JoinColumn(name = "pk")
    private AdEntity ad;
}

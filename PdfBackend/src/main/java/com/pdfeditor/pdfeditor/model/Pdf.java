package com.pdfeditor.pdfeditor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "pdf_models")
public class Pdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    @Lob
    private byte[] content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @ElementCollection
    private List<Integer> pages;


    // Constructors, getters, and setters

    // Constructors
    public Pdf() {
        this.createdAt = new Date();
    }

    public Pdf(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
        this.createdAt = new Date();
    }

    public String getId() {
        return id.toString();
    }
}

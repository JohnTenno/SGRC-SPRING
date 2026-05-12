package com.app.models;

import jakarta.persistence.*;

@Entity
@Table(name = "facultades")
public class Facultad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facultad")
    private Integer idFacultad;

    @Column(name = "nombre_facultad", nullable = false, unique = true, length = 100)
    private String nombreFacultad;

    public Facultad() {
    }

    // Constructor con parámetros
    public Facultad(String nombreFacultad) {
        this.nombreFacultad = nombreFacultad;
    }

    public Integer getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(Integer idFacultad) {
        this.idFacultad = idFacultad;
    }

    public String getNombreFacultad() {
        return nombreFacultad;
    }

    public void setNombreFacultad(String nombreFacultad) {
        this.nombreFacultad = nombreFacultad;
    }
}
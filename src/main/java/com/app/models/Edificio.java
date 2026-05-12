package com.app.models;

import jakarta.persistence.*;

@Entity
@Table(name = "edificios")
public class Edificio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_edificio")
    private Integer idEdificio;

    private String nombre;
    private String ubicacion;

    public Edificio() {}

    public Integer getIdEdificio() { return idEdificio; }
    public void setIdEdificio(Integer idEdificio) { this.idEdificio = idEdificio; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
}
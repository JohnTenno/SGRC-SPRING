package com.app.models;

import jakarta.persistence.*;

@Entity
@Table(name = "cubiculos")
public class Cubiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cubiculo")
    private Integer idCubiculo;

    @ManyToOne
    @JoinColumn(name = "id_edificio", nullable = false)
    private Edificio edificio;

    private String identificador;
    private Integer capacidad;
    private String estado; // DISPONIBLE, MANTENIMIENTO, etc.

    public Cubiculo() {}

    public Integer getIdCubiculo() { return idCubiculo; }
    public void setIdCubiculo(Integer idCubiculo) { this.idCubiculo = idCubiculo; }
    public Edificio getEdificio() { return edificio; }
    public void setEdificio(Edificio edificio) { this.edificio = edificio; }
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
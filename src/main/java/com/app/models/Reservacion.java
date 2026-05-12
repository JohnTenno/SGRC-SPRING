package com.app.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservaciones")
public class Reservacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservacion")
    private Integer idReservacion;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_cubiculo", nullable = false)
    private Cubiculo cubiculo;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estadoReservacion; // PENDIENTE, ACTIVA, etc.

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Reservacion() {}

    public Integer getIdReservacion() { return idReservacion; }
    public void setIdReservacion(Integer idReservacion) { this.idReservacion = idReservacion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Cubiculo getCubiculo() { return cubiculo; }
    public void setCubiculo(Cubiculo cubiculo) { this.cubiculo = cubiculo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public String getEstadoReservacion() { return estadoReservacion; }
    public void setEstadoReservacion(String estadoReservacion) { this.estadoReservacion = estadoReservacion; }
}
package com.app.models;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    // llave foranea

    @ManyToOne 
    @JoinColumn(name = "id_facultad", nullable = false)
    private Facultad facultad;
    // ----------------------------------

    @Column(name = "matricula", nullable = false, unique = true, length = 20)
    private String matricula;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "correo", nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "rol")
    private String rol;


    public Usuario() {
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Facultad getFacultad() { return facultad; }
    public void setFacultad(Facultad facultad) { this.facultad = facultad; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}

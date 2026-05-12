package com.app.repositories; // Ajusta si tu paquete base es distinto

import com.app.models.Facultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultadRepository extends JpaRepository<Facultad, Integer> {
    // ¡Literalmente no tienes que escribir NADA de código aquí adentro!
    // Al heredar de JpaRepository, Spring ya sabe cómo buscar, guardar y borrar facultades.
}
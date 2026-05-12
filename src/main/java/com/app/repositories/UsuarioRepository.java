package com.app.repositories;

import com.app.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Aquí podrías agregar métodos personalizados después, como buscar por matrícula
}
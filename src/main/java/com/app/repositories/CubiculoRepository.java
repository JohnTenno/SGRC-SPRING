package com.app.repositories;

import com.app.models.Cubiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CubiculoRepository extends JpaRepository<Cubiculo, Integer> {
}
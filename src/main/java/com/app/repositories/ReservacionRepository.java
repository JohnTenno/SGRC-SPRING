package com.app.repositories;

import com.app.models.Reservacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservacionRepository extends JpaRepository<Reservacion, Integer> {
}
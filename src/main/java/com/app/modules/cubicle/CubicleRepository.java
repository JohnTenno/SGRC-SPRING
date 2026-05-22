package com.app.modules.cubicle;

import com.app.modules.cubicle.entity.Cubicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CubicleRepository extends JpaRepository<Cubicle, Integer> {
    Optional<Cubicle> findByQrToken(String qrToken);
}

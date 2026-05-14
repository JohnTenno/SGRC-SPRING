package com.app.modules.cubicle;

import com.app.modules.cubicle.entity.Cubicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CubicleRepository extends JpaRepository<Cubicle, Integer> {
}

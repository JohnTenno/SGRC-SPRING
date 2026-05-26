package com.app.modules.cubicle;

import com.app.modules.cubicle.entity.Cubicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CubicleRepository extends JpaRepository<Cubicle, Integer> {

    Optional<Cubicle> findByQrToken(String qrToken);

    @Query("SELECT c FROM Cubicle c WHERE " +
            "(:search IS NULL OR LOWER(c.identifier) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:minCapacity IS NULL OR c.capacity >= :minCapacity) AND " +
            "(:statuses IS NULL OR c.status IN :statuses)")
    Page<Cubicle> findFiltered(
            @Param("search") String search,
            @Param("statuses") List<String> statuses,
            @Param("minCapacity") Integer minCapacity,
            Pageable pageable);
}
package com.app.modules.equipment;

import com.app.modules.equipment.entity.EquipmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Integer> {

    @Query(value = "SELECT e FROM EquipmentType e WHERE " +
                   "(:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                   "(:stockFilter = 'all' OR " +
                   " (:stockFilter = 'in_stock' AND e.totalStock > 0) OR " +
                   " (:stockFilter = 'out_of_stock' AND e.totalStock = 0))",
           countQuery = "SELECT COUNT(e) FROM EquipmentType e WHERE " +
                        "(:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:stockFilter = 'all' OR " +
                        " (:stockFilter = 'in_stock' AND e.totalStock > 0) OR " +
                        " (:stockFilter = 'out_of_stock' AND e.totalStock = 0))")
    Page<EquipmentType> findWithFilters(@Param("search") String search,
                                        @Param("stockFilter") String stockFilter,
                                        Pageable pageable);

    @Query("SELECT e FROM EquipmentType e WHERE " +
           "(:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:stockFilter = 'all' OR " +
           " (:stockFilter = 'in_stock' AND e.totalStock > 0) OR " +
           " (:stockFilter = 'out_of_stock' AND e.totalStock = 0))")
    List<EquipmentType> findWithFiltersAll(@Param("search") String search,
                                           @Param("stockFilter") String stockFilter);
}

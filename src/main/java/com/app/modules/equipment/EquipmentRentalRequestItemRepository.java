package com.app.modules.equipment;

import com.app.modules.equipment.entity.EquipmentRentalRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRentalRequestItemRepository extends JpaRepository<EquipmentRentalRequestItem, Integer> {
    List<EquipmentRentalRequestItem> findByRequestId(Integer requestId);
}

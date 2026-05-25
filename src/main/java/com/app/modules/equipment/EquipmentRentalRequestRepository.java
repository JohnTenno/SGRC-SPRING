package com.app.modules.equipment;

import com.app.modules.equipment.entity.EquipmentRentalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRentalRequestRepository extends JpaRepository<EquipmentRentalRequest, Integer> {
}

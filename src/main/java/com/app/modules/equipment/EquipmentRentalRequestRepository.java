package com.app.modules.equipment;

import com.app.modules.equipment.entity.EquipmentRentalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRentalRequestRepository extends JpaRepository<EquipmentRentalRequest, Integer> {
    List<EquipmentRentalRequest> findByUserId(Integer userId);
    List<EquipmentRentalRequest> findByStatusIn(List<String> statuses);
    List<EquipmentRentalRequest> findAllByOrderByCreatedAtDesc();
    List<EquipmentRentalRequest> findByStatusInOrderByCreatedAtDesc(List<String> statuses);
}

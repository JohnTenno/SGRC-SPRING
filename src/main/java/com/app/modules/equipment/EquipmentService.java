package com.app.modules.equipment;

import com.app.modules.equipment.dto.CreateRentalRequestDto;
import com.app.modules.equipment.dto.EquipmentTypeResponseDto;
import com.app.modules.equipment.dto.RentalRequestResponseDto;
import com.app.modules.equipment.entity.EquipmentRentalRequest;
import com.app.modules.equipment.entity.EquipmentRentalRequestItem;
import com.app.modules.equipment.entity.EquipmentType;
import com.app.modules.user.UserRepository;
import com.app.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private EquipmentRentalRequestRepository rentalRequestRepository;

    @Autowired
    private EquipmentRentalRequestItemRepository rentalRequestItemRepository;

    @Autowired
    private UserRepository userRepository;

    public List<EquipmentTypeResponseDto> getEquipmentTypes() {
        return equipmentTypeRepository.findAll().stream()
                .map(EquipmentTypeResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public RentalRequestResponseDto createRentalRequest(CreateRentalRequestDto dto, String enrollment) {
        User user = userRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<EquipmentType> equipmentList = new ArrayList<>();
        for (CreateRentalRequestDto.ItemDto itemDto : dto.getItems()) {
            EquipmentType equipment = equipmentTypeRepository.findById(itemDto.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found: " + itemDto.getEquipmentId()));
            if (equipment.getAvailableStock() < itemDto.getQuantity()) {
                throw new RuntimeException("Stock not available for: " + equipment.getName());
            }
            equipmentList.add(equipment);
        }

        EquipmentRentalRequest request = new EquipmentRentalRequest();
        request.setUserId(user.getId());
        request = rentalRequestRepository.save(request);
        List<RentalRequestResponseDto.ItemDto> responseItems = new ArrayList<>();
        for (int i = 0; i < dto.getItems().size(); i++) {
            CreateRentalRequestDto.ItemDto itemDto = dto.getItems().get(i);
            EquipmentType equipment = equipmentList.get(i);

            equipment.setAvailableStock(equipment.getAvailableStock() - itemDto.getQuantity());
            equipmentTypeRepository.save(equipment);

            EquipmentRentalRequestItem item = new EquipmentRentalRequestItem();
            item.setRequestId(request.getId());
            item.setEquipmentTypeId(equipment.getId());
            item.setQuantity(itemDto.getQuantity());
            rentalRequestItemRepository.save(item);

            responseItems.add(new RentalRequestResponseDto.ItemDto(
                    equipment.getId(),
                    equipment.getName(),
                    itemDto.getQuantity(),
                    equipment.getAvailableStock(),
                    equipment.getLogoUrl(),
                    equipment.getName()));
        }

        return new RentalRequestResponseDto(request, responseItems);
    }
}

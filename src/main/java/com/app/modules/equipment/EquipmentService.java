package com.app.modules.equipment;

import com.app.modules.equipment.dto.*;
import com.app.modules.equipment.entity.EquipmentRentalRequest;
import com.app.modules.equipment.entity.EquipmentRentalRequestItem;
import com.app.modules.equipment.entity.EquipmentType;
import com.app.modules.user.UserRepository;
import com.app.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public List<EquipmentTypeResponseDto> getEquipmentTypes(String search, String stockFilter) {
        String s = (search != null) ? search.trim() : "";
        String sf = (stockFilter != null && !stockFilter.isBlank()) ? stockFilter : "all";
        return equipmentTypeRepository.findWithFiltersAll(s, sf).stream()
                .map(EquipmentTypeResponseDto::new)
                .collect(Collectors.toList());
    }

    public Page<EquipmentTypeResponseDto> getEquipmentTypesPage(String search, String stockFilter, Pageable pageable) {
        String s = (search != null) ? search.trim() : "";
        String sf = (stockFilter != null && !stockFilter.isBlank()) ? stockFilter : "all";
        return equipmentTypeRepository.findWithFilters(s, sf, pageable).map(EquipmentTypeResponseDto::new);
    }

    @Transactional
    public EquipmentTypeResponseDto createEquipmentType(CreateEquipmentTypeDto dto) {
        EquipmentType e = new EquipmentType();
        e.setName(dto.getName());
        e.setDescription(dto.getDescription());
        e.setTotalStock(dto.getTotalStock() != null ? dto.getTotalStock() : 0);
        e.setLogoUrl(dto.getLogoUrl() != null ? dto.getLogoUrl() : "");
        return new EquipmentTypeResponseDto(equipmentTypeRepository.save(e));
    }

    @Transactional
    public Optional<EquipmentTypeResponseDto> updateEquipmentType(Integer id, UpdateEquipmentTypeDto dto) {
        return equipmentTypeRepository.findById(id).map(e -> {
            if (dto.getName() != null)
                e.setName(dto.getName());
            if (dto.getDescription() != null)
                e.setDescription(dto.getDescription());
            if (dto.getTotalStock() != null)
                e.setTotalStock(dto.getTotalStock());
            if (dto.getLogoUrl() != null)
                e.setLogoUrl(dto.getLogoUrl());
            return new EquipmentTypeResponseDto(equipmentTypeRepository.save(e));
        });
    }

    @Transactional
    public boolean deleteEquipmentType(Integer id) {
        if (!equipmentTypeRepository.existsById(id))
            return false;
        equipmentTypeRepository.deleteById(id);
        return true;
    }

    @Transactional
    public RentalRequestResponseDto createRentalRequest(CreateRentalRequestDto dto, String enrollment) {
        User user = userRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<EquipmentType> equipmentList = new ArrayList<>();
        for (CreateRentalRequestDto.ItemDto itemDto : dto.getItems()) {
            EquipmentType equipment = equipmentTypeRepository.findById(itemDto.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found: " + itemDto.getEquipmentId()));
            if (equipment.getTotalStock() < itemDto.getQuantity()) {
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

            equipment.setTotalStock(equipment.getTotalStock() - itemDto.getQuantity());
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
                    equipment.getTotalStock(),
                    equipment.getLogoUrl(),
                    equipment.getName()));
        }

        return new RentalRequestResponseDto(request, responseItems);
    }

    public List<AdminRentalRequestResponseDto> getAdminRentalRequests(List<String> statuses) {
        List<EquipmentRentalRequest> requests = (statuses != null && !statuses.isEmpty())
                ? rentalRequestRepository.findByStatusInOrderByCreatedAtDesc(statuses)
                : rentalRequestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream().map(this::buildAdminDto).collect(Collectors.toList());
    }

    public List<AdminRentalRequestResponseDto> getStudentRentalRequests(String enrollment) {
        User user = userRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return rentalRequestRepository.findByUserId(user.getId()).stream()
                .map(this::buildAdminDto)
                .collect(Collectors.toList());
    }

    public Optional<AdminRentalRequestResponseDto> getRentalRequestById(Integer id) {
        return rentalRequestRepository.findById(id).map(this::buildAdminDto);
    }

    @Transactional
    public Optional<AdminRentalRequestResponseDto> updateRentalRequestStatus(Integer id, String newStatus) {
        return rentalRequestRepository.findById(id).map(request -> {
            String prevStatus = request.getStatus();
            request.setStatus(newStatus);
            rentalRequestRepository.save(request);

            if ("COMPLETED".equals(newStatus) && !"COMPLETED".equals(prevStatus)) {
                restoreStock(request.getId());
            }

            return buildAdminDto(request);
        });
    }

    private void restoreStock(Integer requestId) {
        List<EquipmentRentalRequestItem> items = rentalRequestItemRepository.findByRequestId(requestId);
        for (EquipmentRentalRequestItem item : items) {
            equipmentTypeRepository.findById(item.getEquipmentTypeId()).ifPresent(e -> {
                e.setTotalStock(e.getTotalStock() + item.getQuantity());
                equipmentTypeRepository.save(e);
            });
        }
    }

    private AdminRentalRequestResponseDto buildAdminDto(EquipmentRentalRequest request) {
        AdminRentalRequestResponseDto dto = new AdminRentalRequestResponseDto();
        dto.setId(request.getId());
        dto.setStatus(request.getStatus());
        dto.setStatusLabel(AdminRentalRequestResponseDto.resolveStatusLabel(request.getStatus()));
        dto.setCreatedAt(request.getCreatedAt() != null
                ? request.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                : null);

        userRepository.findById(request.getUserId()).ifPresent(u -> {
            dto.setStudentName(u.getFullName());
            dto.setStudentEnrollment(u.getEnrollment());
        });

        List<EquipmentRentalRequestItem> rawItems = rentalRequestItemRepository.findByRequestId(request.getId());
        List<AdminRentalRequestResponseDto.ItemDto> itemDtos = new ArrayList<>();
        int totalUnits = 0;
        List<String> summaryParts = new ArrayList<>();

        for (EquipmentRentalRequestItem raw : rawItems) {
            EquipmentType equip = equipmentTypeRepository.findById(raw.getEquipmentTypeId()).orElse(null);
            String typeName = equip != null ? equip.getName() : "Equipo #" + raw.getEquipmentTypeId();
            String image = equip != null ? equip.getLogoUrl() : null;
            itemDtos.add(new AdminRentalRequestResponseDto.ItemDto(raw.getEquipmentTypeId(), typeName,
                    raw.getQuantity(), image));
            totalUnits += raw.getQuantity();
            summaryParts.add(typeName + " x" + raw.getQuantity());
        }

        dto.setItems(itemDtos);
        dto.setTotalUnits(totalUnits);
        dto.setItemsSummary(String.join(", ", summaryParts));
        return dto;
    }
}

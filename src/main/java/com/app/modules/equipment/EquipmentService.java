package com.app.modules.equipment;

import com.app.modules.equipment.dto.*;
import com.app.modules.equipment.entity.EquipmentRentalRequest;
import com.app.modules.equipment.entity.EquipmentRentalRequestItem;
import com.app.modules.equipment.entity.EquipmentType;
import com.app.modules.messaging.publisher.EquipmentEventPublisher;
import com.app.modules.user.UserRepository;
import com.app.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private EquipmentEventPublisher equipmentEventPublisher;

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
        int stock = dto.getTotalStock() != null ? dto.getTotalStock() : 0;
        e.setTotalStock(stock);
        e.setAvailableStock(stock);
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
            if (dto.getTotalStock() != null) {
                int currentlyOut = e.getTotalStock() - e.getAvailableStock();
                int newTotal = dto.getTotalStock();
                e.setTotalStock(newTotal);
                e.setAvailableStock(Math.max(0, newTotal - currentlyOut));
            }
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

        Map<Integer, Integer> aggregated = new LinkedHashMap<>();
        for (CreateRentalRequestDto.ItemDto itemDto : dto.getItems()) {
            aggregated.merge(itemDto.getEquipmentId(), itemDto.getQuantity(), Integer::sum);
        }

        Map<Integer, EquipmentType> equipmentMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : aggregated.entrySet()) {
            EquipmentType equipment = equipmentTypeRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Equipment not found: " + entry.getKey()));
            if (equipment.getAvailableStock() < entry.getValue()) {
                throw new RuntimeException("Stock not available for: " + equipment.getName());
            }
            equipmentMap.put(entry.getKey(), equipment);
        }

        EquipmentRentalRequest request = new EquipmentRentalRequest();
        request.setUserId(user.getId());
        request = rentalRequestRepository.save(request);

        List<RentalRequestResponseDto.ItemDto> responseItems = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : aggregated.entrySet()) {
            EquipmentType equipment = equipmentMap.get(entry.getKey());
            int qty = entry.getValue();

            equipment.setAvailableStock(equipment.getAvailableStock() - qty);
            equipmentTypeRepository.save(equipment);

            EquipmentRentalRequestItem item = new EquipmentRentalRequestItem();
            item.setRequestId(request.getId());
            item.setEquipmentTypeId(equipment.getId());
            item.setQuantity(qty);
            rentalRequestItemRepository.save(item);

            responseItems.add(new RentalRequestResponseDto.ItemDto(
                    equipment.getId(),
                    equipment.getName(),
                    qty,
                    equipment.getAvailableStock(),
                    equipment.getLogoUrl(),
                    equipment.getName()));
        }

        equipmentEventPublisher.publishCreated(request.getId(), user.getId());
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

            equipmentEventPublisher.publishUpdated(request.getId(), request.getUserId(), newStatus);
            return buildAdminDto(request);
        });
    }

    private void restoreStock(Integer requestId) {
        List<EquipmentRentalRequestItem> items = rentalRequestItemRepository.findByRequestId(requestId);
        for (EquipmentRentalRequestItem item : items) {
            equipmentTypeRepository.findById(item.getEquipmentTypeId()).ifPresent(e -> {
                e.setAvailableStock(Math.min(e.getAvailableStock() + item.getQuantity(), e.getTotalStock()));
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

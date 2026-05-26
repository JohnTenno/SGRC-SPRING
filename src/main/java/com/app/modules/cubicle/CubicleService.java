package com.app.modules.cubicle;

import com.app.modules.cubicle.dto.CreateCubicleDto;
import com.app.modules.cubicle.dto.UpdateCubicleDto;
import com.app.modules.cubicle.entity.Cubicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CubicleService {

    @Autowired
    private CubicleRepository cubicleRepository;

    public List<Cubicle> findAll() {
        return cubicleRepository.findAll();
    }

    public Page<Cubicle> findFiltered(String search, List<String> statuses, Integer minCapacity, Pageable pageable) {
        String validSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<String> validStatuses = (statuses != null && !statuses.isEmpty()) ? statuses : null;
        return cubicleRepository.findFiltered(validSearch, validStatuses, minCapacity, pageable);
    }

    public Optional<Cubicle> findById(Integer id) {
        return cubicleRepository.findById(id);
    }

    public Cubicle create(CreateCubicleDto dto) {
        Cubicle cubicle = new Cubicle();
        cubicle.setIdentifier(dto.getIdentifier());
        cubicle.setCapacity(dto.getCapacity() != null ? dto.getCapacity() : 6);
        cubicle.setStatus(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE");
        cubicle.setLogoUrl(dto.getLogoUrl() != null ? dto.getLogoUrl() : "");
        cubicle.setQrToken(UUID.randomUUID().toString());
        return cubicleRepository.save(cubicle);
    }

    public Optional<Cubicle> update(Integer id, UpdateCubicleDto dto) {
        return cubicleRepository.findById(id).map(cubicle -> {
            if (dto.getIdentifier() != null)
                cubicle.setIdentifier(dto.getIdentifier());
            if (dto.getCapacity() != null)
                cubicle.setCapacity(dto.getCapacity());
            if (dto.getStatus() != null)
                cubicle.setStatus(dto.getStatus());
            if (dto.getLogoUrl() != null)
                cubicle.setLogoUrl(dto.getLogoUrl());
            return cubicleRepository.save(cubicle);
        });
    }

    public boolean delete(Integer id) {
        if (!cubicleRepository.existsById(id))
            return false;
        cubicleRepository.deleteById(id);
        return true;
    }
}
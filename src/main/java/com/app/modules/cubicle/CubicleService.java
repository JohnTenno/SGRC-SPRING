package com.app.modules.cubicle;

import com.app.modules.building.BuildingRepository;
import com.app.modules.cubicle.dto.CreateCubicleDto;
import com.app.modules.cubicle.dto.UpdateCubicleDto;
import com.app.modules.cubicle.entity.Cubicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CubicleService {

    @Autowired
    private CubicleRepository cubicleRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    public List<Cubicle> findAll() {
        return cubicleRepository.findAll();
    }

    public Optional<Cubicle> findById(Integer id) {
        return cubicleRepository.findById(id);
    }

    public Cubicle create(CreateCubicleDto dto) {
        Cubicle cubicle = new Cubicle();
        buildingRepository.findById(dto.getBuildingId()).ifPresent(cubicle::setBuilding);
        cubicle.setIdentifier(dto.getIdentifier());
        cubicle.setCapacity(dto.getCapacity());
        cubicle.setStatus(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE");
        return cubicleRepository.save(cubicle);
    }

    public Optional<Cubicle> update(Integer id, UpdateCubicleDto dto) {
        return cubicleRepository.findById(id).map(cubicle -> {
            if (dto.getBuildingId() != null)
                buildingRepository.findById(dto.getBuildingId()).ifPresent(cubicle::setBuilding);
            if (dto.getIdentifier() != null)
                cubicle.setIdentifier(dto.getIdentifier());
            if (dto.getCapacity() != null)
                cubicle.setCapacity(dto.getCapacity());
            if (dto.getStatus() != null)
                cubicle.setStatus(dto.getStatus());
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

package com.app.modules.building;

import com.app.modules.building.dto.CreateBuildingDto;
import com.app.modules.building.dto.UpdateBuildingDto;
import com.app.modules.building.entity.Building;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    public List<Building> findAll() {
        return buildingRepository.findAll();
    }

    public Optional<Building> findById(Integer id) {
        return buildingRepository.findById(id);
    }

    public Building create(CreateBuildingDto dto) {
        Building building = new Building();
        building.setName(dto.getName());
        building.setLocation(dto.getLocation());
        return buildingRepository.save(building);
    }

    public Optional<Building> update(Integer id, UpdateBuildingDto dto) {
        return buildingRepository.findById(id).map(building -> {
            if (dto.getName() != null)
                building.setName(dto.getName());
            if (dto.getLocation() != null)
                building.setLocation(dto.getLocation());
            return buildingRepository.save(building);
        });
    }

    public boolean delete(Integer id) {
        if (!buildingRepository.existsById(id))
            return false;
        buildingRepository.deleteById(id);
        return true;
    }
}

package com.app.modules.building;

import com.app.modules.building.dto.BuildingResponseDto;
import com.app.modules.building.dto.CreateBuildingDto;
import com.app.modules.building.dto.UpdateBuildingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @GetMapping
    public List<BuildingResponseDto> getAll() {
        return buildingService.findAll().stream()
                .map(BuildingResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildingResponseDto> getById(@PathVariable Integer id) {
        return buildingService.findById(id)
                .map(BuildingResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public BuildingResponseDto create(@RequestBody CreateBuildingDto dto) {
        return new BuildingResponseDto(buildingService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuildingResponseDto> update(@PathVariable Integer id, @RequestBody UpdateBuildingDto dto) {
        return buildingService.update(id, dto)
                .map(BuildingResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return buildingService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

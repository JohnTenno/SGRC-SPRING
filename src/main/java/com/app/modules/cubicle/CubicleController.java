package com.app.modules.cubicle;

import com.app.modules.cubicle.dto.CreateCubicleDto;
import com.app.modules.cubicle.dto.CubicleResponseDto;
import com.app.modules.cubicle.dto.UpdateCubicleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cubicles")
public class CubicleController {

    @Autowired
    private CubicleService cubicleService;

    @GetMapping
    public List<CubicleResponseDto> getAll() {
        return cubicleService.findAll().stream()
                .map(CubicleResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CubicleResponseDto> getById(@PathVariable Integer id) {
        return cubicleService.findById(id)
                .map(CubicleResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CubicleResponseDto create(@RequestBody CreateCubicleDto dto) {
        return new CubicleResponseDto(cubicleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CubicleResponseDto> update(@PathVariable Integer id, @RequestBody UpdateCubicleDto dto) {
        return cubicleService.update(id, dto)
                .map(CubicleResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return cubicleService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

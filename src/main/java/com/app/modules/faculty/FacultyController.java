package com.app.modules.faculty;

import com.app.modules.faculty.dto.CreateFacultyDto;
import com.app.modules.faculty.dto.FacultyResponseDto;
import com.app.modules.faculty.dto.UpdateFacultyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/faculties")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    @GetMapping
    public List<FacultyResponseDto> getAll() {
        return facultyService.findAll().stream()
                .map(FacultyResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyResponseDto> getById(@PathVariable Integer id) {
        return facultyService.findById(id)
                .map(FacultyResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public FacultyResponseDto create(@RequestBody CreateFacultyDto dto) {
        return new FacultyResponseDto(facultyService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyResponseDto> update(@PathVariable Integer id, @RequestBody UpdateFacultyDto dto) {
        return facultyService.update(id, dto)
                .map(FacultyResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return facultyService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}

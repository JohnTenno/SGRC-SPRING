package com.app.modules.tutoring;

import com.app.modules.tutoring.dto.SubjectRequestDto;
import com.app.modules.tutoring.dto.SubjectResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutoring/subjects")
public class TutoringSubjectController {

    @Autowired
    private TutoringSubjectService subjectService;

    @GetMapping
    public ResponseEntity<List<SubjectResponseDto>> getAll() {
        return ResponseEntity.ok(subjectService.getAll());
    }

    @PostMapping
    public ResponseEntity<SubjectResponseDto> create(@RequestBody SubjectRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDto> update(@PathVariable Integer id, @RequestBody SubjectRequestDto dto) {
        return subjectService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            return subjectService.delete(id)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}

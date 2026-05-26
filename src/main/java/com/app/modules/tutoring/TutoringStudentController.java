package com.app.modules.tutoring;

import com.app.modules.tutoring.dto.StudentResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutoring/students")
public class TutoringStudentController {

    @Autowired
    private TutoringProfessorService professorService;

    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getStudents() {
        return ResponseEntity.ok(professorService.getStudents());
    }

    @PostMapping("/{enrollment}/promote")
    public ResponseEntity<StudentResponseDto> promote(@PathVariable String enrollment) {
        return professorService.promoteStudent(enrollment)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{enrollment}/demote")
    public ResponseEntity<StudentResponseDto> demote(@PathVariable String enrollment) {
        return professorService.demoteStudent(enrollment)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

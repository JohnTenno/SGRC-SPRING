package com.app.modules.faculty;

import com.app.modules.faculty.dto.CreateFacultyDto;
import com.app.modules.faculty.dto.UpdateFacultyDto;
import com.app.modules.faculty.entity.Faculty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacultyService {

    @Autowired
    private FacultyRepository facultyRepository;

    public List<Faculty> findAll() {
        return facultyRepository.findAll();
    }

    public Optional<Faculty> findById(Integer id) {
        return facultyRepository.findById(id);
    }

    public Faculty create(CreateFacultyDto dto) {
        return facultyRepository.save(new Faculty(dto.getName()));
    }

    public Optional<Faculty> update(Integer id, UpdateFacultyDto dto) {
        return facultyRepository.findById(id).map(faculty -> {
            if (dto.getName() != null)
                faculty.setName(dto.getName());
            return facultyRepository.save(faculty);
        });
    }

    public boolean delete(Integer id) {
        if (!facultyRepository.existsById(id))
            return false;
        facultyRepository.deleteById(id);
        return true;
    }
}

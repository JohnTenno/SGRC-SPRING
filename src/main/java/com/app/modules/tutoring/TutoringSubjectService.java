package com.app.modules.tutoring;

import com.app.modules.tutoring.dto.SubjectRequestDto;
import com.app.modules.tutoring.dto.SubjectResponseDto;
import com.app.modules.tutoring.entity.TutoringSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TutoringSubjectService {

    @Autowired
    private TutoringSubjectRepository subjectRepository;

    public List<SubjectResponseDto> getAll() {
        return subjectRepository.findAll().stream()
                .map(SubjectResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubjectResponseDto create(SubjectRequestDto dto) {
        TutoringSubject s = new TutoringSubject();
        s.setName(dto.getName());
        s.setDescription(dto.getDescription());
        return new SubjectResponseDto(subjectRepository.save(s));
    }

    @Transactional
    public Optional<SubjectResponseDto> update(Integer id, SubjectRequestDto dto) {
        return subjectRepository.findById(id).map(s -> {
            if (dto.getName() != null)
                s.setName(dto.getName());
            if (dto.getDescription() != null)
                s.setDescription(dto.getDescription());
            return new SubjectResponseDto(subjectRepository.save(s));
        });
    }

    @Transactional
    public boolean delete(Integer id) {
        if (!subjectRepository.existsById(id))
            return false;
        subjectRepository.deleteById(id);
        return true;
    }
}

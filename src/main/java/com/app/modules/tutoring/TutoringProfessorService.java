package com.app.modules.tutoring;

import com.app.modules.tutoring.dto.*;
import com.app.modules.tutoring.entity.TutoringOffering;
import com.app.modules.tutoring.entity.TutoringProfessor;
import com.app.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TutoringProfessorService {

    @Autowired
    private TutoringProfessorRepository professorRepository;

    @Autowired
    private TutoringOfferingRepository offeringRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ProfessorResponseDto> getAll() {
        return professorRepository.findAll().stream()
                .map(ProfessorResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfessorResponseDto create(ProfessorRequestDto dto) {
        TutoringProfessor p = new TutoringProfessor();
        p.setEmployeeNumber(dto.getEmployeeNumber());
        p.setFullName(dto.getFullName());
        p.setBio(dto.getBio());
        p = professorRepository.save(p);

        TutoringOffering offering = new TutoringOffering();
        offering.setEmployeeNumber(p.getEmployeeNumber());
        offering.setAvailableWeekdays(new ArrayList<>());
        offering.setSubjectIds(new ArrayList<>());
        offeringRepository.save(offering);

        return new ProfessorResponseDto(p);
    }

    @Transactional
    public Optional<ProfessorResponseDto> update(String employeeNumber, ProfessorRequestDto dto) {
        return professorRepository.findById(employeeNumber).map(p -> {
            if (dto.getFullName() != null)
                p.setFullName(dto.getFullName());
            if (dto.getBio() != null)
                p.setBio(dto.getBio());
            return new ProfessorResponseDto(professorRepository.save(p));
        });
    }

    @Transactional
    public boolean delete(String employeeNumber) {
        if (!professorRepository.existsById(employeeNumber))
            return false;
        offeringRepository.deleteById(employeeNumber);
        professorRepository.deleteById(employeeNumber);
        return true;
    }

    public Optional<OfferingResponseDto> getOffering(String employeeNumber) {
        return offeringRepository.findById(employeeNumber).map(OfferingResponseDto::new);
    }

    @Transactional
    public Optional<OfferingResponseDto> updateOffering(String employeeNumber, OfferingRequestDto dto) {
        return offeringRepository.findById(employeeNumber).map(o -> {
            if (dto.getScheduleSummary() != null)
                o.setScheduleSummary(dto.getScheduleSummary());
            if (dto.getTutoringLocation() != null)
                o.setTutoringLocation(dto.getTutoringLocation());
            if (dto.getAvailableWeekdays() != null)
                o.setAvailableWeekdays(dto.getAvailableWeekdays());
            if (dto.getSubjectIds() != null)
                o.setSubjectIds(dto.getSubjectIds());
            return new OfferingResponseDto(offeringRepository.save(o));
        });
    }

    public List<StudentResponseDto> getStudents() {
        return userRepository.findAll().stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .map(StudentResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<StudentResponseDto> promoteStudent(String enrollment) {
        return userRepository.findByEnrollment(enrollment).map(u -> {
            u.setTutor(true);
            return new StudentResponseDto(userRepository.save(u));
        });
    }

    @Transactional
    public Optional<StudentResponseDto> demoteStudent(String enrollment) {
        return userRepository.findByEnrollment(enrollment).map(u -> {
            u.setTutor(false);
            return new StudentResponseDto(userRepository.save(u));
        });
    }
}

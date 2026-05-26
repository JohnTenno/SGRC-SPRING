package com.app.modules.tutoring;

import com.app.modules.tutoring.dto.TutoringRequestResponseDto;
import com.app.modules.tutoring.dto.TutoringRequestSubmitDto;
import com.app.modules.tutoring.entity.TutoringRequest;
import com.app.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TutoringRequestService {

    @Autowired
    private TutoringRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public TutoringRequestResponseDto submit(TutoringRequestSubmitDto dto, String enrollment) {
        userRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TutoringRequest r = new TutoringRequest();
        r.setStudentEnrollment(enrollment);
        r.setProfessorEmployeeNumber(dto.getProfessorEmployeeNumber());
        r.setSubject(dto.getSubject());
        r.setReservationDate(dto.getReservationDate());
        r.setStartTime(dto.getStartTime());
        r.setEndTime(dto.getEndTime());
        r.setTopic(dto.getTopic());
        return new TutoringRequestResponseDto(requestRepository.save(r));
    }

    public List<TutoringRequestResponseDto> getMyRequests(String enrollment) {
        return requestRepository.findByStudentEnrollment(enrollment).stream()
                .map(TutoringRequestResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<TutoringRequestResponseDto> getAll() {
        return requestRepository.findAll().stream()
                .map(TutoringRequestResponseDto::new)
                .collect(Collectors.toList());
    }
}

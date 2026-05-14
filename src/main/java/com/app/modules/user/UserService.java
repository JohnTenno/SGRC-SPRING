package com.app.modules.user;

import com.app.modules.faculty.FacultyRepository;
import com.app.modules.user.dto.CreateUserDto;
import com.app.modules.user.dto.UpdateUserDto;
import com.app.modules.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public User create(CreateUserDto dto) {
        User user = new User();
        facultyRepository.findById(dto.getFacultyId()).ifPresent(user::setFaculty);
        user.setEnrollment(dto.getEnrollment());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole() != null ? dto.getRole() : "STUDENT");
        return userRepository.save(user);
    }

    public Optional<User> update(Integer id, UpdateUserDto dto) {
        return userRepository.findById(id).map(user -> {
            if (dto.getFacultyId() != null)
                facultyRepository.findById(dto.getFacultyId()).ifPresent(user::setFaculty);
            if (dto.getEnrollment() != null)
                user.setEnrollment(dto.getEnrollment());
            if (dto.getFullName() != null)
                user.setFullName(dto.getFullName());
            if (dto.getEmail() != null)
                user.setEmail(dto.getEmail());
            if (dto.getRole() != null)
                user.setRole(dto.getRole());
            return userRepository.save(user);
        });
    }

    public boolean delete(Integer id) {
        if (!userRepository.existsById(id))
            return false;
        userRepository.deleteById(id);
        return true;
    }
}

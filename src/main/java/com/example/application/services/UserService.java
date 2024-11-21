package com.example.application.services;

import com.example.application.data.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                      StudentRepository studentRepository,
                      LecturerRepository lecturerRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String username, String email, String firstName, 
                           String lastName, String password, UserType userType,
                           String studentNumber, String staffNumber, String department) {
        // Validate unique constraints
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setHashedPassword(passwordEncoder.encode(password));
        user.setUserType(userType);

        // Set roles based on user type
        Set<Role> roles = new HashSet<>();
        if (userType == UserType.STUDENT) {
            roles.add(Role.ROLE_STUDENT);
        } else if (userType == UserType.LECTURER) {
            roles.add(Role.ROLE_LECTURER);
        }
        user.setRoles(roles);

        user = userRepository.save(user);

        // Create specific user details
        if (userType == UserType.STUDENT) {
            if (studentRepository.existsByStudentNumber(studentNumber)) {
                throw new RuntimeException("Student number already exists");
            }
            Student student = new Student();
            student.setUser(user);
            student.setStudentNumber(studentNumber);
            studentRepository.save(student);
        } else if (userType == UserType.LECTURER) {
            if (lecturerRepository.existsByStaffNumber(staffNumber)) {
                throw new RuntimeException("Staff number already exists");
            }
            Lecturer lecturer = new Lecturer();
            lecturer.setUser(user);
            lecturer.setStaffNumber(staffNumber);
            lecturer.setDepartment(department);
            lecturerRepository.save(lecturer);
        }

        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
package com.lll.zjgsu.coursecloud.user.service;

import com.lll.zjgsu.coursecloud.user.model.Student;
import com.lll.zjgsu.coursecloud.user.model.Teacher;
import com.lll.zjgsu.coursecloud.user.model.User;
import com.lll.zjgsu.coursecloud.user.repository.StudentRepository;
import com.lll.zjgsu.coursecloud.user.repository.TeacherRepository;
import com.lll.zjgsu.coursecloud.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    public UserService(StudentRepository studentRepository,
                       TeacherRepository teacherRepository,
                       UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> getTeacherById(String id) {
        return teacherRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }

    public void deleteTeacher(String id) {
        teacherRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> getTeacherByTeacherId(String teacherId) {
        return teacherRepository.findByTeacherId(teacherId);
    }

    public Student updateStudent(Student student) {
        return studentRepository.save(student);
    }

    /**
     * 根据用户名查找用户（支持学生和教师）
     * @param username 用户名
     * @return User 对象，如果找不到则返回 null
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return User 对象，如果找不到则返回 null
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return boolean
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return boolean
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
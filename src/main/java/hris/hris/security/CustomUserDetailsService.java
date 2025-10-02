package hris.hris.security;

import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Employee not found with email: {}", email);
                    return new UsernameNotFoundException("Employee not found with email: " + email);
                });

        if (!employee.getIsActive()) {
            log.warn("Employee account is inactive: {}", email);
            throw new UsernameNotFoundException("Employee account is inactive: " + email);
        }

        // Determine roles based on employee data
        java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));

        // If employee has subordinates, they are a supervisor
        if (employeeRepository.existsBySupervisorId(employee.getId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPERVISOR"));
        }

        // For now, assume all active employees are also HR and ADMIN for testing
        // In production, this should be based on actual department/role data
        if (employee.getEmail().contains("admin") || employee.getEmail().contains("hr")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_HR"));
        }

        return User.builder()
                .username(employee.getEmail())
                .password(employee.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!employee.getIsActive())
                .build();
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with email: " + email));
    }
}
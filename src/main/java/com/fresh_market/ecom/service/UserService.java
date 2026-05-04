package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // for password hashing
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId((UUID) rs.getObject("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setRole(rs.getString("role"));
        user.setPhoneNumber(rs.getString("phone"));
        user.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }
        if (updated != null) {
            user.setUpdatedAt(updated.toLocalDateTime());
        }
        return user;
    };

    @Override
    public List<User> getUsers() {
        String sql = "select * from users order by created_at desc";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public User getUserById(UUID id) {
        String sql = "select * from users where id = ?::uuid";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    @Override
    public User createUser(User user) {
        String sql = """
                INSERT INTO users (email, password_hash, first_name, last_name, role, phone, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING *
                """;
        String hashedPassword = passwordEncoder.encode(user.getPasswordHash());

        return jdbcTemplate.queryForObject(
                sql,
                userRowMapper,
                user.getEmail(),
                hashedPassword,
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole() : "CUSTOMER",
                user.getPhoneNumber(),
                user.isActive()
        );
    }

    @Override
    public User updateUser(UUID id, User user) {
        String sql = """
                UPDATE users
                SET email = ?, first_name = ?, last_name = ?, phone = ?, role = ?, is_active = ?, updated_at = NOW()
                WHERE id = ?
                RETURNING *
                """;
        return jdbcTemplate.queryForObject(
                sql,
                userRowMapper,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive(),
                id
        );
    }

    @Override
    public void deleteUser(UUID id) {
        String sql = "delete from users where id = ?::uuid";
        jdbcTemplate.update(sql, id);
    }
}

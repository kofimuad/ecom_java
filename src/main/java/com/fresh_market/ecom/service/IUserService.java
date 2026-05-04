package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.User;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    List<User> getUsers();
    User getUserById(UUID id);
    User createUser(User user);
    User updateUser(UUID id, User user);
    void deleteUser(UUID id);
}

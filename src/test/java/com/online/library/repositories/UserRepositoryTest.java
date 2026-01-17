package com.online.library.repositories;

import com.online.library.domain.entities.UserEntity;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository underTest;

    @Test
    public void testThatUserCanBeCreatedAndRecalled() {
        UserEntity user = TestDataUtil.createTestUser();
        underTest.save(user);
        Optional<UserEntity> result = underTest.findById(user.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void testThatFindByUsernameReturnsUser() {
        UserEntity user = TestDataUtil.createTestUser();
        underTest.save(user);
        Optional<UserEntity> result = underTest.findByUsername(user.getUsername());
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void testThatFindByEmailReturnsUser() {
        UserEntity user = TestDataUtil.createTestUser();
        underTest.save(user);
        Optional<UserEntity> result = underTest.findByEmail(user.getEmail());
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void testThatUserCanBeDeleted() {
        UserEntity user = TestDataUtil.createTestUser();
        underTest.save(user);
        underTest.deleteById(user.getId());
        Optional<UserEntity> result = underTest.findById(user.getId());
        assertThat(result).isNotPresent();
    }
}

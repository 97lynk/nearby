package io.a97lynk.nearby.websocket.repository;

import io.a97lynk.nearby.websocket.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
}

package io.a97lynk.nearby.repository;

import io.a97lynk.nearby.entity.Friendship;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @EntityGraph(attributePaths = {"account", "friend"})
    List<Friendship> findAllByAccountIdIn(Set<String> accountIds);
}

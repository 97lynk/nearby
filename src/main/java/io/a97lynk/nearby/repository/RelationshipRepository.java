package io.a97lynk.nearby.repository;

import io.a97lynk.nearby.entity.Relationship;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    @EntityGraph(attributePaths = {"account", "friend"})
    List<Relationship> findAllByAccountIdIn(Set<String> accountIds);
}

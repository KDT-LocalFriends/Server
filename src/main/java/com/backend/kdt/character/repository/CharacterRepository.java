package com.backend.kdt.character.repository;

import com.backend.kdt.character.entity.Character;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {

    Optional<Character> findByUserId(Long userId);

    @Query("SELECT c FROM Character c WHERE c.user.id = :userId")
    Optional<Character> findCharacterByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Character c WHERE c.level >= :minLevel")
    List<Character> findByMinLevel(@Param("minLevel") Integer minLevel);
}
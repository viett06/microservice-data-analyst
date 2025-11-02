package com.viet.data.repository;

import com.viet.data.module.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends MongoRepository<Dataset, String> {

    Page<Dataset> findByUserId(String userId, Pageable pageable);

    Optional<Dataset> findByIdAndUserId(String id, String userId);

    List<Dataset> findByUserIdAndStatus(String userId, Dataset.DatasetStatus status);

    @Query("{ 'user_id': ?0 }")
    List<Dataset> findAllByUserId(String userId);

    @Query(value = "{ 'user_id': ?0 }", count = true)
    long countByUserId(String userId);

    @Query("{ 'user_id': ?0, 'status': ?1 }")
    Page<Dataset> findByUserIdAndStatus(String userId, Dataset.DatasetStatus status, Pageable pageable);

    @Query(value = "{ 'user_id': ?0 }", delete = true)
    void deleteAllByUserId(String userId);
}

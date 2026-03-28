package com.example.trasuaweb_backend.repositories;

import com.example.trasuaweb_backend.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'approved'")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r.id) FROM Review r WHERE r.product.id = :productId AND r.status = 'approved'")
    Integer countApprovedReviewsByProductId(@Param("productId") Long productId);
}

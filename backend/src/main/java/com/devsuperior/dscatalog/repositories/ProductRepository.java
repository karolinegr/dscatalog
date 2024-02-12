package com.devsuperior.dscatalog.repositories;

import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select distinct obj from Product obj " +
            "inner join obj.categories as cats where " +
            "(:categories IS NULL OR cats IN (:categories)) AND " +
            "(lower(obj.name) like lower(concat('%',:name,'%'))) ")
    Page<Product> find(
            List<Category> categories,
            String name,
            Pageable pageable);

    @Query("select obj from Product obj join fetch obj.categories where " +
            "obj in :products")
    List<Product> findProductsWithCategories(List<Product> products);
}

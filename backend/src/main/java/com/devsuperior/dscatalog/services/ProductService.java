package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(
            Long categoryId,
            String name,
            Pageable pageRequest) {
        List<Category> categories = categoryId == 0 ? null : List.of(categoryRepository.getReferenceById(categoryId));
        Page<Product> pagedList = this.productRepository.find(categories, name, pageRequest);
        productRepository.findProductsWithCategories(pagedList.getContent());
        return pagedList.map(item -> new ProductDTO(item, item.getCategories()));
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> obj = this.productRepository.findById(id);
        Product product = obj.orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        return new ProductDTO(product, product.getCategories());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO newProduct){
        Product newProductEntity = new Product();
        this.copyDtoToEntity(newProduct, newProductEntity);
        newProductEntity = this.productRepository.save(newProductEntity);
        return new ProductDTO(newProductEntity, newProductEntity.getCategories());
    }

    @Transactional
    public ProductDTO updateProduct(
            Long id,
            ProductDTO newProduct){
        try {
            Product productEntity = this.productRepository.getReferenceById(id);
            this.copyDtoToEntity(newProduct, productEntity);
            productEntity = this.productRepository.save(productEntity);
            return new ProductDTO(productEntity, productEntity.getCategories());
        } catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Id not found: " + id);
        }
    }

    public void deleteProduct(
            Long id){
        try{
            this.productRepository.deleteById(id);
        } catch(EmptyResultDataAccessException e){
            throw new ResourceNotFoundException("Id not found: " + id);
        } catch(DataIntegrityViolationException e){
            throw new DatabaseException("Integrity violation");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product entity){
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());
        entity.setDate(dto.getDate());
        entity.getCategories().clear();

        for (CategoryDTO categoryDTO : dto.getCategories()){
            Category category = this.categoryRepository.getReferenceById(categoryDTO.getId());
            entity.getCategories().add(category);
        }
    }
}

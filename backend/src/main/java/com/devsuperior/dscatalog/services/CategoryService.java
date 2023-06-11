package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        List<Category> list = this.categoryRepository.findAll();
        return list.stream().map(CategoryDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Optional<Category> obj = this.categoryRepository.findById(id);
        Category category = obj.orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        return new CategoryDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO newCategory){
        Category newCategoryEntity = new Category();
        newCategoryEntity.setName(newCategory.getName());
        newCategoryEntity = this.categoryRepository.save(newCategoryEntity);
        return new CategoryDTO(newCategoryEntity);
    }

    @Transactional
    public CategoryDTO updateCategory(
            Long id,
            CategoryDTO newCategory){
        try {
            Category categoryEntity = this.categoryRepository.getReferenceById(id);
            categoryEntity.setName(newCategory.getName());
            categoryEntity = this.categoryRepository.save(categoryEntity);
            return new CategoryDTO(categoryEntity);
        } catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Id not found: " + id);
        }
    }
}

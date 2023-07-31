package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;
    private PageImpl<Product> page;
    private Product product;
    private Category category;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 4L;
        product = Factory.createProduct();
        productDTO = Factory.createProductDto();
        page = new PageImpl<>(List.of(product));
        category = Factory.createCategory();

        Mockito.when(productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

        Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.when(productRepository.getReferenceById(existingId)).thenReturn(product);
        Mockito.doThrow(EntityNotFoundException.class).when(productRepository).getReferenceById(nonExistingId);

        Mockito.when(categoryRepository.getReferenceById(existingId)).thenReturn(category);
        Mockito.doThrow(EntityNotFoundException.class).when(categoryRepository).getReferenceById(nonExistingId);

        Mockito.doThrow(ResourceNotFoundException.class).when(productRepository).findById(nonExistingId);
        Mockito.doNothing().when(productRepository).deleteById(existingId);
        Mockito.doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(nonExistingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);
    }

    @Test
    public void findAllPagedShouldReturnPage(){
        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductDTO> result = service.findAllPaged(pageable);
        Assertions.assertNotNull(result);
        Mockito.verify(productRepository, Mockito.times(1)).findAll(pageable);
    }

    @Test
    public void findByIdShouldReturnProdutDTOWhenExistingId(){
        ProductDTO result = service.findById(existingId);

        Assertions.assertNotNull(result);
        Mockito.verify(productRepository, Mockito.times(1)).findById(existingId);
    }

    @Test
    public void findByIdShouldThrowExceptionWhenIdDoesNotExist(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingId);
        });
        Mockito.verify(productRepository, Mockito.times(1)).findById(nonExistingId);
    }

    @Test
    public void updateShouldUpdateProductAndReturnProductDtoWhenExistingId(){
        ProductDTO result = service.updateProduct(existingId, productDTO);
        Assertions.assertNotNull(result);
        Mockito.verify(productRepository, Mockito.times(1)).getReferenceById(existingId);
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(categoryRepository, Mockito.times(1)).getReferenceById(existingId);
    }

    @Test
    public void updateShouldThrowExceptionWhenIdDoesNotExist(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.updateProduct(nonExistingId, productDTO);
        });

        Mockito.verify(productRepository, Mockito.times(1)).getReferenceById(nonExistingId);
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists(){
        Assertions.assertDoesNotThrow(() -> {
            service.deleteProduct(existingId);
        });
        Mockito.verify(productRepository, Mockito.times(1)).deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.deleteProduct(nonExistingId);
        });
        Mockito.verify(productRepository, Mockito.times(1)).deleteById(nonExistingId);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId(){
        Assertions.assertThrows(DatabaseException.class, () -> {
            service.deleteProduct(dependentId);
        });
        Mockito.verify(productRepository, Mockito.times(1)).deleteById(dependentId);
    }

}

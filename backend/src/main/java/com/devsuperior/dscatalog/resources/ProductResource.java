package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping(value = "/products")
public class ProductResource {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> findAll(Pageable pageable) {
        Page<ProductDTO> findAllPaged = this.productService.findAllPaged(pageable);

        return ResponseEntity.ok().body(findAllPaged);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> findById(
            @PathVariable Long id) {
        ProductDTO productDto = this.productService.findById(id);
        return ResponseEntity.ok().body(productDto);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO newProduct){
        ProductDTO newProductDto = this.productService.createProduct(newProduct);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newProductDto.getId()).toUri();
        return ResponseEntity.created(uri).body(newProductDto);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productUpdate){
        ProductDTO updatedProduct = this.productService.updateProduct(id, productUpdate);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id){
        this.productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

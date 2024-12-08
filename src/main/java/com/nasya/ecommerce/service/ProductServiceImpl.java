package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.ResourceNotFoundException;
import com.nasya.ecommerce.entity.Category;
import com.nasya.ecommerce.entity.Product;
import com.nasya.ecommerce.entity.ProductCategory;
import com.nasya.ecommerce.entity.ProductCategory.ProductCategoryId;
import com.nasya.ecommerce.model.request.product.ProductRequest;
import com.nasya.ecommerce.model.response.category.CategoryResponse;
import com.nasya.ecommerce.model.response.product.ProductResponse;
import com.nasya.ecommerce.repository.CategoryRepository;
import com.nasya.ecommerce.repository.ProductCategoryRepository;
import com.nasya.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;


    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(product -> {
                    List<CategoryResponse> categoryResponses = getProductCategoires(product.getProductId());
                    return ProductResponse.fromProductAndCategories(product, categoryResponses);
                })
                .toList();
    }

    @Override
    public Page<ProductResponse> findByPage(Pageable pageable) {
        return productRepository.findByPageable(pageable).map(product ->{
            List<CategoryResponse> categoryResponses = getProductCategoires(product.getProductId());
            return ProductResponse.fromProductAndCategories(product, categoryResponses);
        });

    }

    @Override
    public ProductResponse findById(Long productId) {
        //get the product from the product table where productId
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found with id: " +productId));
        // get the category response
        List<CategoryResponse> categoryResponses = getProductCategoires(productId);

        return ProductResponse.fromProductAndCategories(product, categoryResponses);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        List<Category> categories = getCategoriesByIds(request.getCategoryIds());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .weight(request.getWeight())
                .build();

        Product saveProduct = productRepository.save(product);

        List<ProductCategory> productCategories = categories.stream()
                .map(category -> {
                    ProductCategoryId productCategoryId = new ProductCategoryId();
                    productCategoryId.setCategoryId(category.getCategoryId());
                    productCategoryId.setProductId(saveProduct.getProductId());
                    return ProductCategory.builder()
                            .id(productCategoryId)
                            .build();
                }).toList();
        productCategoryRepository.saveAll(productCategories);

        List<CategoryResponse> categoryResponseList = categories.stream()
                .map(CategoryResponse::fromCategory)
                .toList();

        return ProductResponse.fromProductAndCategories(saveProduct, categoryResponseList);
    }

    @Override
    @Transactional
    public ProductResponse update(Long productId, ProductRequest request) {

        //get the product that want to update from product table
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found with id: " + productId));

        //update the product, agument: from the request
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setWeight(request.getWeight());
        productRepository.save(existingProduct);


        // get category of the product from category table
        List<Category> categories = getCategoriesByIds(request.getCategoryIds());

        // get category of product and delete the category
        List<ProductCategory> existingProductCategories = productCategoryRepository
                .findCategoiresByProductId(productId);
        productCategoryRepository.deleteAll(existingProductCategories);

        //save new category into product_category table from request
        List<ProductCategory> productCategories = categories.stream()
                .map(category -> {
                    ProductCategoryId productCategoryId = new ProductCategoryId();
                    productCategoryId.setCategoryId(category.getCategoryId());
                    productCategoryId.setProductId(productId);
                    return ProductCategory.builder()
                            .id(productCategoryId)
                            .build();
                }).toList();
        productCategoryRepository.saveAll(productCategories);

        //create category response
        List<CategoryResponse> categoryResponseList = categories.stream()
                .map(CategoryResponse::fromCategory)
                .toList();

        return ProductResponse.fromProductAndCategories(existingProduct,categoryResponseList);
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        //get the product that want to delete from product table
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found with id: " + productId));

        //get product categories from product_category table
        List<ProductCategory> productCategories = productCategoryRepository.findCategoiresByProductId(productId);

        productCategoryRepository.deleteAll(productCategories);
        productRepository.delete(existingProduct);
    }

    /***
     * This function is used to get categories from the category table.
     * @param categoryIds;
     * @return categories
     */
    private List<Category> getCategoriesByIds(List<Long> categoryIds) {
        return categoryIds.stream()
                .map(categoryId -> categoryRepository.findById((categoryId))
                        .orElseThrow(()-> new ResourceNotFoundException("CATEGORY NOT FOUND FOR ID: "+categoryId)))
                .toList();
    }

    /**
     * This function is used to get productCategory record and then transform it to CategoryResponse
     *
     * @param productId;
     * @return List of Category Response
     */
    private List<CategoryResponse> getProductCategoires(Long productId) {
        List<ProductCategory> productCategories = productCategoryRepository.findCategoiresByProductId(productId);
        List<Long> categoryIds = productCategories.stream()
                .map(productCategory -> productCategory.getId().getCategoryId())
                .toList();

        return categoryRepository.findAllById(categoryIds).stream()
                .map(CategoryResponse::fromCategory)
                .toList();
    }
}

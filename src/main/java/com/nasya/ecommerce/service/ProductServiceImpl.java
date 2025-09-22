package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.ResourceNotFoundException;
import com.nasya.ecommerce.entity.Category;
import com.nasya.ecommerce.entity.Product;
import com.nasya.ecommerce.entity.ProductCategory;
import com.nasya.ecommerce.entity.ProductCategory.ProductCategoryId;
import com.nasya.ecommerce.model.request.product.ProductRequest;
import com.nasya.ecommerce.model.response.category.CategoryResponse;
import com.nasya.ecommerce.model.response.product.PaginatedProductResponse;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private final String PRODUCT_CACHE_KEY = "products:";
    private final CacheService cacheService;


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
    public Page<ProductResponse> findByNameAndPageable(String name, Pageable pageable) {
        name = "%" + name.toLowerCase() + "%";
        return productRepository.findByNamePageable(name, pageable).map(product ->{
            List<CategoryResponse> categoryResponses = getProductCategoires(product.getProductId());
            return ProductResponse.fromProductAndCategories(product, categoryResponses);
        });
    }

    @Override
    public ProductResponse findById(Long productId) {
        String cacheKey = PRODUCT_CACHE_KEY + productId;
        // search product on redis first
        Optional<ProductResponse> cachedProduct = cacheService.get(cacheKey, ProductResponse.class);
        if(cachedProduct.isPresent()){
            // return product data if the data already cached
            return cachedProduct.get();
        }

        //get the product from the product table where productId
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found with id: " +productId));
        // get the category response
        List<CategoryResponse> categoryResponses = getProductCategoires(productId);

        ProductResponse productResponse =  ProductResponse.fromProductAndCategories(product, categoryResponses);
        // save the product to redis
        cacheService.put(cacheKey, productResponse);
        return productResponse;
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
                .userId(request.getUser().getUserId())
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

        //save product to cache (REDIS)
        String cacheKey = PRODUCT_CACHE_KEY + saveProduct.getProductId();
        cacheService.put(cacheKey, categoryResponseList);

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


        String cacheKey = PRODUCT_CACHE_KEY + productId;
        cacheService.evict(cacheKey);

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

    @Override
    public PaginatedProductResponse convertProductPage(Page<ProductResponse> response) {
        return PaginatedProductResponse.builder()
                .data(response.getContent())
                .pageNo(response.getNumber())
                .pageSize(response.getSize())
                .totalElements(response.getNumberOfElements())
                .totalPages(response.getTotalPages())
                .last(response.isLast())
                .build();
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

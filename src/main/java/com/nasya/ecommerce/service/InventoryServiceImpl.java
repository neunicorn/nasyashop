package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.InventoryException;
import com.nasya.ecommerce.entity.Product;
import com.nasya.ecommerce.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public boolean checkAndLockInventory(Map<Long, Integer> productQuantities) {
        for(Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(()-> new InventoryException("Product is not found "));

            if(product.getStockQuantity() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public void decreaseQuantity(Map<Long, Integer> productQuantities) {
        for(Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(()-> new InventoryException("Product is not found "));

            if(product.getStockQuantity() < entry.getValue()) {
                throw new InventoryException("Stock quantity exceeded");
            }
            Integer newQuantity = product.getStockQuantity() - entry.getValue();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        }

    }

    @Override
    @Transactional
    public void increaseQuantity(Map<Long, Integer> productQuantities) {
        for(Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(()-> new InventoryException("Product is not found "));

            Integer newQuantity = product.getStockQuantity() + entry.getValue();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        }
    }
}

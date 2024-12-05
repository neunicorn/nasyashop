package com.nasya.ecommerce.model.request.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Nama product tidak boleh kosong")
    @Size(min = 2, max = 100, message = "Nama produk harus antara 2 sampai 100 karakter")
    private String name;

    @NotNull(message = "Harga produk tidak boleh kosong")
    @Positive(message = "Harga produk harus bernilai positif")
    @Digits(integer = 10, fraction = 2, message = "Harga harus memiliki maksimal 10 digit dan 2 angka dibelakang koma")
    private BigDecimal price;

    @NotNull(message = "Deskripsi produk tidak boleh null")
    @Size(max = 100, message = "Deskripsi produk tidak boleh lebih dari 100 karakter")
    private String description;
}

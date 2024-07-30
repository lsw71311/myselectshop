package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    public static final int MIN_MY_PRICE=100;

    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        Product product = productRepository.save(new Product(requestDto, user));
        return new ProductResponseDto(product);
    }

    @Transactional //더티체킹 -> 변경 감지 후 수정
    public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        int myprice = requestDto.getMyprice();

        if(myprice < MIN_MY_PRICE){
            throw new IllegalArgumentException("유효하지 않은 관심 가격. 최소 "+MIN_MY_PRICE+"원 이상으로 설정하세요.");
        }

        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException(("해당 상품을 찾을 수 없습니다.")));

        product.update(requestDto);

        return new ProductResponseDto(product);
    }

    @Transactional(readOnly = true)//->productResponsedto에서 폴더리스트 가져올떄 지연로딩 기능 가능
    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;    //오름, 내림차순 정렬 방향 결정
        Sort sort = Sort.by(direction, sortBy); //정렬의 기준이 될 컬럼(sortby)과 방향을 정의
        Pageable pageable = PageRequest.of(page, size, sort); // 페이지네이션과 정렬을 함께 처리할 수 있는 객체

        UserRoleEnum userRoleEnum = user.getRole();

        Page<Product> productList;

        //admin, user 권한 확인
        if(userRoleEnum == UserRoleEnum.USER){
            productList = productRepository.findAllByUser(user, pageable);
        }else{
            productList = productRepository.findAll(pageable);
        }

        return productList.map(ProductResponseDto::new);
    }

    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 상품이 존재하지 않습니다.")
        );
        product.updateByItemDto(itemDto);

    }

    //admin 계정이면 모든 회원의 모든 관심 상품 조회
//    public List<ProductResponseDto> getAllProducts() {
//        List<Product> productList = productRepository.findAll();
//        List<ProductResponseDto> responseDtoList = new ArrayList<>();
//
//        for (Product product : productList) {
//            responseDtoList.add(new ProductResponseDto(product));
//        }
//
//        return responseDtoList;
//    }
}

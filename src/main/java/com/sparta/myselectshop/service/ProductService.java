package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FolderRepository folderRepository;
    private final ProductFolderRepository productFolderRepository;

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

    public void addFolder(Long productId, Long folderId, User user) {

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new NullPointerException("해당 상품이 존재하지 않습니다. ")
        );

        Folder folder = folderRepository.findById(folderId).orElseThrow(
                () -> new NullPointerException("해당 폴더가 존재하지 않습니다.")
        );

        if(!product.getUser().getId().equals(user.getId())
        || !folder.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("회원님의 관심 상품이 아니거나, 회원님의 폴더가 아니기에 폴더 추가할 수 없습니다.");
        }

        Optional<ProductFolder> overlapFolder = productFolderRepository.findByProductAndFolder(product, folder);

        if(overlapFolder.isPresent()){
            throw new IllegalArgumentException("중복된 폴더입니다.");
        }

        productFolderRepository.save(new ProductFolder(product, folder));  //엔티티클래스 객체 하나가 db에서 한줄이 된다.
    }

    //각 폴더에 담긴 관심상품 조회
    public Page<ProductResponseDto> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
        //페이징 처리
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;    //오름, 내림차순 정렬 방향 결정
        Sort sort = Sort.by(direction, sortBy); //정렬의 기준이 될 컬럼(sortby)과 방향을 정의
        Pageable pageable = PageRequest.of(page, size, sort); // 페이지네이션과 정렬을 함께 처리할 수 있는 객체

        Page<Product> productList = productRepository.findAllByUserAndProductFolderList_FolderId(user, folderId, pageable);

        Page<ProductResponseDto> responseDtoList = productList.map(ProductResponseDto::new);

        return responseDtoList;
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

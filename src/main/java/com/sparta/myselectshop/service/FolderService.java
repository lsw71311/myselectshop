package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.FolderResponseDto;
import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    public void addFolders(List<String> folderNames, User user) {

        //폴더이름으로 존재하는지 조회
        List<Folder> existFolderList = folderRepository.findAllByUserAndNameIn(user, folderNames);
        
        List<Folder> folderList = new ArrayList<>();

        for (String folderName : folderNames) {
            if(!isExistFolderName(folderName, existFolderList)){ //기존의 폴더 이름과 중복 x면
                Folder folder = new Folder(folderName, user);
                folderList.add(folder);
            }else{
                throw new IllegalArgumentException("이미 존재하는 폴더명입니다.");
            }
        }

        folderRepository.saveAll(folderList);
        
    }

    public List<FolderResponseDto> getFolders(User user) {
        List<Folder> folderList = folderRepository.findAllByUser(user);
        List<FolderResponseDto> responseDtoList = new ArrayList<>();

        for (Folder folder : folderList) {
            responseDtoList.add(new FolderResponseDto(folder));
        }

        return responseDtoList;
    }

    private boolean isExistFolderName(String folderName, List<Folder> existFolderList) {
        for (Folder existfolder : existFolderList) {
            if(folderName.equals(existfolder.getName())){
                return true;
            }
        }
        return false;
    }

}

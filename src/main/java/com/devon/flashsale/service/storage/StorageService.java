package com.devon.flashsale.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadEventImage(MultipartFile file);

    void deleteEventImage(String imageUrl);
    
}
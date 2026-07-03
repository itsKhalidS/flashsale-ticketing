package com.devon.flashsale.service.storage.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.devon.flashsale.exceptions.FileStorageException;
import com.devon.flashsale.service.storage.StorageService;
import com.devon.flashsale.validation.EventValidator;

import jakarta.annotation.PostConstruct;

@Service
public class LocalStorageService implements StorageService {

	@Value("${app.storage.location}")
	private String storageLocation;

	
	@Value("${app.storage.public.path}")
	private String publicPath;
	
	private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(storageLocation);
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Failed to initialize upload directory", e);
        }
    }
	
	@Override
	public String uploadEventImage(MultipartFile file) {
		List<FileStorageException> exceptions = EventValidator.validateEventImage(file);
		if(exceptions.size() > 0) {
			throw exceptions.get(0);
		}
		
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID() + extension;
        Path destination = rootLocation.resolve(uniqueFilename);
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store image.", e);
        }
        return publicPath + "/" + uniqueFilename;
	}

	@Override
	public void deleteEventImage(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String filename = Paths.get(imageUrl).getFileName().toString();
        Path file = rootLocation.resolve(filename);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete image.", e);
        }
	}

}

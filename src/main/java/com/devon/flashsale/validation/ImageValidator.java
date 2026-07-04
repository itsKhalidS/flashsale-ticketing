package com.devon.flashsale.validation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import com.devon.flashsale.exceptions.FileStorageException;

public class ImageValidator {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of( "image/jpeg", "image/png", "image/webp"
            );

    private static final double EXPECTED_RATIO = 4.0 / 5.0;
    private static final double RATIO_TOLERANCE = 0.03;

    public static List<FileStorageException> validate(MultipartFile file) {

        List<FileStorageException> exceptions = new java.util.ArrayList<>();
        if (file == null || file.isEmpty()) {
            exceptions.add(new FileStorageException("Event image is required."));
            return exceptions;
        }
        validateExtension(file, exceptions);
        validateContentType(file, exceptions);
        validateImage(file, exceptions);

        return exceptions;
    }

    private static void validateExtension(MultipartFile file, List<FileStorageException> exceptions) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            exceptions.add(new FileStorageException("Invalid file name."));
            return;
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            exceptions.add(new FileStorageException("Only JPG, JPEG, PNG and WEBP images are allowed."));
        }
    }

    private static void validateContentType(MultipartFile file, List<FileStorageException> exceptions) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            exceptions.add(new FileStorageException("Invalid image type."));
        }
    }

    private static void validateImage( MultipartFile file, List<FileStorageException> exceptions) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                exceptions.add(new FileStorageException("Uploaded file is not a valid image."));
                return;
            }
            double ratio = (double) image.getWidth() / image.getHeight();

            if (Math.abs(ratio - EXPECTED_RATIO) > RATIO_TOLERANCE) {
                exceptions.add( new FileStorageException("Image must have an aspect ratio close to 4:5."));
            }
        } catch (IOException e) {
            exceptions.add(new FileStorageException("Unable to read uploaded image."));
        }
    }
}
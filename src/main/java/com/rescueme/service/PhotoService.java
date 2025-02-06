package com.rescueme.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PhotoService {
    List<String> processPhotos(List<MultipartFile> photos, Long petId) throws IOException;
}



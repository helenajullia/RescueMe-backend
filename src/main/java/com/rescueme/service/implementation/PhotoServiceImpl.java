package com.rescueme.service.implementation;

import com.rescueme.service.PhotoService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhotoServiceImpl implements PhotoService {

    @Override
    public List<String> processPhotos(List<MultipartFile> photos, Long petId) throws IOException {
        List<String> photoUrls = new ArrayList<>();
        String baseDir = System.getProperty("user.dir") + "/uploads/pets/" + petId;


        File directory = new File(baseDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory: " + baseDir);
        }


        for (MultipartFile photo : photos) {
            String fileName = photo.getOriginalFilename();


            if (fileName != null) {
                String normalizedFileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                File destinationFile = new File(directory, normalizedFileName);

                photo.transferTo(destinationFile);

                photoUrls.add("/uploads/pets/" + petId + "/" + normalizedFileName);
            }
        }
        return photoUrls;
    }

}




package com.rescueme.repository.dto;

import com.rescueme.repository.entity.PetPhoto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class PetPhotoDTO {
    private Long id;
    private String url;

    public static PetPhotoDTO toDto(PetPhoto petPhoto) {
        return new PetPhotoDTO(
                petPhoto.getId(),
                "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(petPhoto.getPhotoData())
        );
    }

    public static List<PetPhotoDTO> toDtoList(List<PetPhoto> petPhotos) {
        return petPhotos.stream().map(PetPhotoDTO::toDto).collect(Collectors.toList());
    }
}


package com.rescueme.service;

import com.rescueme.repository.PetPhotoRepository;
import com.rescueme.repository.PetRepository;
import com.rescueme.repository.dto.PetResponseDTO;
import com.rescueme.repository.dto.PetStatsDTO;
import com.rescueme.repository.entity.Pet;
import com.rescueme.repository.entity.PetStatus;
import com.rescueme.repository.entity.User;
import com.rescueme.service.implementation.PetPhotoServiceImpl;
import com.rescueme.service.implementation.PetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PetServiceImplTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetPhotoService petPhotoService;

    @InjectMocks
    private PetServiceImpl petService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllPets() {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Max");
        pet.setStatus(PetStatus.AVAILABLE);
        User shelter = new User();
        shelter.setId(10L);
        pet.setShelter(shelter);

        Pet pet2 = new Pet();
        pet2.setId(1L);
        pet2.setName("Luna");
        pet2.setStatus(PetStatus.AVAILABLE);
        User shelter2 = new User();
        shelter2.setId(8L);
        pet2.setShelter(shelter2);

        when(petRepository.findAll()).thenReturn(List.of(pet, pet2));
        List<PetResponseDTO> result = petService.getAllPets();

        assertEquals(2, result.size());
        assertEquals("Max", result.get(0).getName());
        assertEquals("Luna", result.get(1).getName());
    }

    @Test
    void testGetPetById() {
        Pet pet = new Pet();
        pet.setId(2L);
        pet.setName("Luna");

        when(petRepository.findById(2L)).thenReturn(Optional.of(pet));

        Pet result = petService.getPetById(2L);

        assertEquals("Luna", result.getName());
    }

    @Test
    void testDeletePetByShelterId_success() {
        Pet pet = new Pet();
        pet.setId(3L);
        User shelter = new User();
        shelter.setId(20L);
        pet.setShelter(shelter);

        when(petRepository.findById(3L)).thenReturn(Optional.of(pet));

        boolean deleted = petService.deletePetByShelterId(20L, 3L);

        assertTrue(deleted);
        verify(petRepository).deleteById(3L);
    }

    @Test
    void testGetPetsByShelterId() {
        Pet pet = new Pet();
        pet.setId(4L);
        pet.setName("Charlie");
        User shelter = new User();
        shelter.setId(5L);
        pet.setShelter(shelter);
        pet.setStatus(PetStatus.AVAILABLE);

        when(petRepository.findByShelterId(5L)).thenReturn(List.of(pet));

        List<PetResponseDTO> result = petService.getPetsByShelterId(5L);

        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getName());
    }

    @Test
    void testGetPetStatsByShelter() {
        when(petRepository.countByShelterId(100L)).thenReturn(10L);
        when(petRepository.countByShelterIdAndUrgentAdoptionNeededTrue(100L)).thenReturn(2);
        when(petRepository.countByShelterIdAndStatus(100L, PetStatus.ADOPTED)).thenReturn(3);
        when(petRepository.countByShelterIdAndStatus(100L, PetStatus.PENDING)).thenReturn(1);
        when(petRepository.countByShelterIdAndStatus(100L, PetStatus.AVAILABLE)).thenReturn(6);

        PetStatsDTO stats = petService.getPetStatsByShelter(100L);

        assertEquals(3, stats.getAdopted());
        assertEquals(1, stats.getPending());
        assertEquals(6, stats.getAvailable());
        assertEquals(10, stats.getTotal());
        assertEquals(2, stats.getUrgent());
    }

    @Test
    void testGetAllBreeds() {
        when(petRepository.findDistinctBreeds()).thenReturn(List.of("Labrador", "Beagle"));
        List<String> breeds = petService.getAllBreeds();
        assertEquals(2, breeds.size());
    }

    @Test
    void testGetBreedsBySpecies() {
        when(petRepository.findDistinctBreedsBySpecies("dog")).thenReturn(List.of("Poodle", "Bulldog"));
        List<String> result = petService.getBreedsBySpecies("dog");
        assertEquals(2, result.size());
        assertTrue(result.contains("Poodle"));
    }

    @Test
    void testCountPetsByShelter() {
        when(petRepository.countByShelterId(55L)).thenReturn(7L);
        long count = petService.countPetsByShelter(55L);
        assertEquals(7L, count);
    }

    @Test
    void testGetPetsByStatus() {
        Pet pet = new Pet();
        pet.setId(6L);
        pet.setName("Rocky");
        pet.setStatus(PetStatus.AVAILABLE);
        User shelter = new User();
        shelter.setId(9L);
        pet.setShelter(shelter);

        when(petRepository.findByStatus(PetStatus.AVAILABLE)).thenReturn(List.of(pet));

        List<PetResponseDTO> result = petService.getPetsByStatus(PetStatus.AVAILABLE);

        assertEquals(1, result.size());
        assertEquals("Rocky", result.get(0).getName());
    }

    @Test
    void testUpdatePet_successful() {
        Long petId = 1L;
        Long shelterId = 10L;

        Pet existingPet = new Pet();
        existingPet.setId(petId);
        existingPet.setName("Lolu");
        existingPet.setStatus(PetStatus.AVAILABLE);

        User shelter = new User();
        shelter.setId(shelterId);
        existingPet.setShelter(shelter);

        Pet updatedData = new Pet();
        updatedData.setName("Lola");
        updatedData.setAge(5.0);
        updatedData.setStatus(PetStatus.ADOPTED);
        updatedData.setVaccinated(true);
        updatedData.setNeutered(true);
        updatedData.setUrgentAdoptionNeeded(true);
        updatedData.setStory("This is the story of Lola");

        MultipartFile photo1 = mock(MultipartFile.class);
        List<MultipartFile> newPhotos = List.of(photo1);

        List<Long> photoIdsToDelete = List.of(100L, 101L);

        when(petRepository.findById(petId)).thenReturn(Optional.of(existingPet));
        when(petRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pet result = petService.updatePet(petId, updatedData, shelterId, newPhotos, photoIdsToDelete);

        assertEquals("Lola", result.getName());
        assertEquals(PetStatus.ADOPTED, result.getStatus());
        assertTrue(result.isVaccinated());
        assertTrue(result.isNeutered());
        assertTrue(result.isUrgentAdoptionNeeded());
        assertEquals("This is the story of Lola", result.getStory());

        verify(petPhotoService).deletePhotoById(100L);
        verify(petPhotoService).deletePhotoById(101L);
        verify(petPhotoService).addPhotosToPet(eq(existingPet), eq(newPhotos));
        verify(petRepository).save(existingPet);
    }

}

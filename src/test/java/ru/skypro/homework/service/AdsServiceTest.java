package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exception.AdNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdEntityRepository;
import ru.skypro.homework.repository.UserEntityRepository;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AdsServiceTest {
    @Mock
    private AdEntityRepository adEntityRepository;
    @Mock
    private UserEntityRepository userEntityRepository;
    @Mock
    private AdMapper adMapper;
    @Mock
    private ImageService imageService;
    @Mock
    private Principal principal;
    @InjectMocks
    private AdService adService;
    Ad AD = new Ad();
    UserEntity USER = new UserEntity();
    MockMultipartFile FILE = new MockMultipartFile(
            "image",
            "image.png",
            "image/png",
            "Test Image".getBytes());
    ImageEntity IMAGE = new ImageEntity();

    CreateOrUpdateAd createOrUpdateAd = new CreateOrUpdateAd("Кроссовки", 5000, "Крутые-кроссы");
    AdEntity adEntity = new AdEntity();


    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        AD.setPk(1);
        AD.setTitle("Кроссовки");
        AD.setPrice(5000);
        AD.setAuthor(1);

        USER.setFirstName("User");
        USER.setLastName("User");
        USER.setEmail("user@mail.ru");
        USER.setPhone("+7(000)000-00-00");

        IMAGE.setData(FILE.getBytes());

        adEntity.setUser(USER);
        adEntity.setImage(IMAGE);
        adEntity.setPk(AD.getPk());
        adEntity.setDescription(createOrUpdateAd.getDescription());
        adEntity.setTitle(createOrUpdateAd.getTitle());
    }


    @Test
    void testSaveAd() throws IOException {
        when(userEntityRepository.findByEmail(principal.getName())).thenReturn(Optional.of(USER));
        when(adMapper.mapCreateOrUpdateDtoTo(createOrUpdateAd)).thenReturn(adEntity);
        when(adService.saveAd(createOrUpdateAd, FILE, principal)).thenReturn(AD);

        Ad result = adService.saveAd(createOrUpdateAd, FILE, principal);

        assertEquals(AD, result);
        verify(adMapper, times(2)).mapCreateOrUpdateDtoTo(createOrUpdateAd);
        verify(userEntityRepository, times(2)).findByEmail(principal.getName());
    }

    @Test
    void testGetAllAds() {
        List<Ad> adList = List.of(AD);
        Ads ads = new Ads();
        ads.setCount(adList.size());
        ads.setResults(adList);

        List<AdEntity> list = List.of(adEntity);

        when(adEntityRepository.findAll()).thenReturn(list);
        when(adMapper.mapToListDto(list)).thenReturn(List.of(AD));


        Ads result = adService.getAllAds();

        assertEquals(ads, result);
        verify(adMapper, times(1)).mapToListDto(list);
        verify(adEntityRepository, times(1)).findAll();
    }

    @Test
    void testGetInformationAboutAd() throws AdNotFoundException {
        ExtendedAd extendedAd = new ExtendedAd();
        extendedAd.setPrice(AD.getPrice());
        extendedAd.setPk(AD.getPk());
        extendedAd.setPhone(USER.getPhone());
        extendedAd.setEmail(USER.getEmail());
        extendedAd.setTitle(AD.getTitle());
        extendedAd.setImage(FILE.toString());
        extendedAd.setDescription(createOrUpdateAd.getDescription());

        when(adEntityRepository.findById(any(Integer.class))).thenReturn(Optional.of(adEntity));
        when(adMapper.mapToExtAdDto(adEntity)).thenReturn(extendedAd);

        ExtendedAd result = adService.getInformationAboutAd(AD.getPk());

        assertEquals(extendedAd, result);
        verify(adMapper, times(1)).mapToExtAdDto(adEntity);
        verify(adEntityRepository, times(1)).findById(any(Integer.class));
    }
    @Test
    void testDeleteAd() throws AdNotFoundException{
        when(adEntityRepository.findById(any(Integer.class))).thenReturn(Optional.of(adEntity));

        adService.deleteAd(AD.getPk());

        verify(adEntityRepository, times(1)).deleteById(any(Integer.class));
        verify(adEntityRepository, times(1)).findById(any(Integer.class));
    }

}

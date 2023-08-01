package ru.skypro.homework.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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

@Service
@Transactional
public class AdService {
    private final AdEntityRepository adRepository;
    private final UserEntityRepository userEntityRepository;
    private final AdMapper adMapper;
    private final ImageService imageService;


    public AdService(AdEntityRepository adRepository, UserEntityRepository userEntityRepository, AdMapper adMapper, ImageService imageService) {
        this.adRepository = adRepository;
        this.userEntityRepository = userEntityRepository;
        this.adMapper = adMapper;
        this.imageService = imageService;
    }

    public Ad saveAd(CreateOrUpdateAd createOrUpdateAd, MultipartFile image, Principal principal) throws IOException {
        String email = principal.getName();
        Optional<UserEntity> optionalUserEntity = userEntityRepository.findByEmail(email);
        UserEntity userEntity = optionalUserEntity.get();
        AdEntity adEntity = adMapper.mapCreateOrUpdateDtoTo(createOrUpdateAd);
        ImageEntity uploadImage = imageService.saveImageForAd(adEntity, image);
        adEntity.setImage(uploadImage);
        adEntity.setUser(userEntity);
        adRepository.save(adEntity);
        return adMapper.mapToDto(adEntity);
    }

    public Ads getAllAds() {
        List<AdEntity> list = adRepository.findAll();
        List<Ad> adDtoList = adMapper.mapToListDto(list);
        Ads ads = new Ads();
        ads.setCount(adDtoList.size());
        ads.setResults(adDtoList);
        return ads;
    }

    public ExtendedAd getInformationAboutAd(int idAd) throws AdNotFoundException {
        Optional<AdEntity> optionalAdlEntity = adRepository.findById(idAd);
        if (optionalAdlEntity.isPresent()) {
            AdEntity adEntity = optionalAdlEntity.get();
           return adMapper.mapToExtAdDto(adEntity);
        }
        throw new AdNotFoundException();
    }
    @PreAuthorize(value = "hasRole('ADMIN') or @adService.checkAccessForAd(principal.username, #id)")
    public void deleteAd(int idAd) throws AdNotFoundException {
        Optional<AdEntity> optionalAdEntity = adRepository.findById(idAd);
        if (optionalAdEntity.isPresent()) {
            AdEntity adEntity = optionalAdEntity.get();
            adRepository.deleteById(adEntity.getPk());
        } else {
            throw new AdNotFoundException();
        }
    }
    @PreAuthorize("hasRole('ADMIN') or @adService.checkAccessForAd(principal.username, #id)")
    public Ad updateAD(int adId, CreateOrUpdateAd createOrUpdateAd) throws AdNotFoundException {
        Optional<AdEntity> optionalAdlEntity = adRepository.findById(adId);
        if (optionalAdlEntity.isPresent()) {
            AdEntity adEntity = optionalAdlEntity.get();
            adEntity.setTitle(createOrUpdateAd.getTitle());
            adEntity.setPrice(createOrUpdateAd.getPrice());
            adEntity.setDescription(createOrUpdateAd.getDescription());
            adRepository.save(adEntity);
            return adMapper.mapToDto(adEntity);
        } else {
            throw new AdNotFoundException();
        }
    }

    public Ads getAllAdsByUser(Principal principal) throws AdNotFoundException {
        Optional<UserEntity> optionalUserEntity = userEntityRepository.findByEmail(principal.getName());
        System.out.println(principal.getName());
        if (optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();
            System.out.println(userEntity.getEmail());
            List<AdEntity> adEntity = adRepository.findAllByUserId(userEntity.getId());
            System.out.println(adEntity.size());
            List<Ad> adsListDto = adMapper.mapToListDto(adEntity);
            Ads ads = new Ads();
            ads.setCount(adsListDto.size());
            ads.setResults(adsListDto);
            return ads;
        } else {
            throw new AdNotFoundException();
        }
    }
    public void updateImage(int adId, MultipartFile file) throws IOException {
        Optional<AdEntity> optionalAdlEntity = adRepository.findById(adId);
        if (optionalAdlEntity.isPresent()){
            AdEntity adEntity = optionalAdlEntity.get();
            imageService.updateImageForAd(adEntity, file);
        } else {
            throw new IOException();
        }
    }

    public boolean checkAccessForAd(String username, Integer id) {
        Optional<AdEntity> optional = adRepository.findById(id);
        return optional.map(adEntity -> adEntity.getUser().getUsername().equals(username)).orElse(false);
    }

}


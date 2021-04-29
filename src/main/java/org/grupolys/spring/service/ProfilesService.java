package org.grupolys.spring.service;

import org.grupolys.hazelcast.HazelcastCache;
import org.grupolys.spring.model.mappers.DictionaryProfileMapper;
import org.grupolys.spring.model.payloads.DictionaryProfilePayload;
import org.grupolys.spring.model.payloads.ProfileDictionaryPayload;
import org.grupolys.spring.model.persistence.PersistentDictionary;
import org.grupolys.spring.model.persistence.PersistentDictionaryProfile;
import org.grupolys.spring.model.responses.DictionaryProfileResponse;
import org.grupolys.spring.model.responses.ErrorResponse;
import org.grupolys.spring.repositories.ProfilesRepository;
import org.grupolys.spring.service.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfilesService {

    private final ProfilesRepository profilesRepository;
    private final DictionaryProfileMapper mapper;
    private final HazelcastCache cache;

    @Autowired
    ProfilesService(ProfilesRepository profilesRepository,
                    DictionaryProfileMapper mapper,
                    @Lazy HazelcastCache cache) {
        this.profilesRepository = profilesRepository;
        this.mapper = mapper;
        this.cache = cache;
    }
    /**
     * Returns the profile that has id `profileId`.
     * @param profileId profile id
     * @return the profile.
     */
    public PersistentDictionaryProfile getProfile(String profileId) throws ServiceException {
        PersistentDictionaryProfile profile = profilesRepository.findById(profileId).orElse(null);

        if (profile == null) {
            throw new ServiceException(
                    new ErrorResponse("Profile not found: profile with id '" + profileId + "' was not found.",
                            HttpStatus.NOT_FOUND
                    )
            );
        }

        return profile;
    }

    /**
     * Returns the profile that has id `profileId` and has the dictionary `dictionaryId`
     * @param profileId profile id
     * @param dictionaryId dictionary id
     * @return the profile.
     */
    public PersistentDictionaryProfile getProfile(String profileId, String dictionaryId) throws ServiceException {
        PersistentDictionaryProfile profile = profilesRepository.findByProfileAndDictionary(profileId, dictionaryId);

        if (profile == null) {
            throw new ServiceException(
                    new ErrorResponse("Profile not found: profile with id '" + profileId + "' was not found.",
                            HttpStatus.NOT_FOUND
                    )
            );
        }

        return profile;
    }

    /**
     * Returns all the profiles.
     * @return all the profiles
     */
    public List<PersistentDictionaryProfile> getProfiles() {
        return profilesRepository.findAll();
    }

    /**
     * Returns true if a profile with name `profileName` exists.
     * @param profileName the name of the profile
     * @return true if the profile exists with the requested name. False otherwise.
     */
    public boolean profileWithNameExists(String profileName) {
        return profilesRepository.findByName(profileName) != null;
    }

    /**
     * Returns a profile based on the argument `profileName`
     * @param profileName the name for the profile to look for
     * @return the persistent profile
     */
    public PersistentDictionaryProfile getProfileByName(String profileName) {
        return profilesRepository.findByName(profileName);
    }

    /**
     * Saves a profile into the db.
     * @param payload payload to save.
     * @return returns the saved persistent mapped object.
     */
    public DictionaryProfileResponse saveProfile(DictionaryProfilePayload payload) throws ServiceException {
        if (profileWithNameExists(payload.getName())) {
            throw new ServiceException(
                    new ErrorResponse("A profile with name '" + payload.getName() + "' already exists.",
                            HttpStatus.CONFLICT
                    )
            );
        }

        return mapper.toDictionaryProfileResponse(
                profilesRepository.insert(
                        mapper.toPersistentDictionaryProfile(payload)
                )
        );
    }


    /**
     * Stores a dictionary profile in the database.
     * @param profileId the profile id where to store the dictionary
     * @param payload the dictionary payload
     * @return the persistent version of the dictionary
     * @throws ServiceException if a dictionary with the same name already exists.
     */
    public PersistentDictionary createDictionaryForProfile(String profileId, ProfileDictionaryPayload payload) throws ServiceException {
        PersistentDictionaryProfile profile = getProfile(profileId);

        if (profile.getDictionaries().stream()
                .filter(d -> d.getName().equals(payload.getName())).findAny().orElse(null) != null) {
            throw new ServiceException(
                    new ErrorResponse("A dictionary with the same name '" + payload.getName() + "'" +
                            " already exists.", HttpStatus.CONFLICT)
            );
        } else {
            PersistentDictionary dictionary = mapper.toPersistentDictionary(payload);
            // if there's no other dict, set this dictionary as the active directory
            if (profile.getDictionaries().size() == 0) {
                profile.setActiveDictionary(dictionary.getId());
            }
            profile.getDictionaries().add(dictionary);
            profilesRepository.save(profile);
            cache.onDictionaryCreated(dictionary.getId());
            return dictionary;
        }
    }
}

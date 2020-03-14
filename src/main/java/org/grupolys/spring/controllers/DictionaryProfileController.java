package org.grupolys.spring.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.grupolys.hazelcast.HazelcastCache;
import org.grupolys.profiles.exception.DictionaryNotFoundException;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.processor.Processor;
import org.grupolys.spring.model.mappers.DictionaryProfileMapper;
import org.grupolys.spring.model.payloads.AnalyzePayload;
import org.grupolys.spring.model.payloads.DictionaryProfilePayload;
import org.grupolys.spring.model.payloads.ProfileDictionaryPayload;
import org.grupolys.spring.model.payloads.UpdateProfilePayload;
import org.grupolys.spring.model.persistence.PersistentAnalysis;
import org.grupolys.spring.model.persistence.PersistentDictionary;
import org.grupolys.spring.model.persistence.PersistentDictionaryProfile;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.grupolys.spring.model.responses.DictionaryProfileResponse;
import org.grupolys.spring.repositories.ProfilesRepository;
import org.grupolys.spring.repositories.SentimentRepository;
import org.grupolys.spring.repositories.WordsRepository;
import org.grupolys.spring.service.AnalyzeService;
import org.grupolys.spring.service.SamulanProcessorService;
import org.grupolys.spring.service.SamulanRulesService;
import org.grupolys.spring.validations.IsSupportedLanguage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
public class DictionaryProfileController {

    @Autowired
    private HazelcastCache cache;

    @Autowired
    private DictionaryProfileMapper mapper;

    @Autowired
    private WordsRepository wordsRepository;

    @Autowired
    private ProfilesRepository profilesRepository;

    @Autowired
    private SentimentRepository sentimentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalyzeService analyzeService;

    @GetMapping("/profiles")
    public ResponseEntity<List<DictionaryProfileResponse>> getProfiles() {
        HttpStatus httpStatus = HttpStatus.OK;
        List<PersistentDictionaryProfile> profiles = profilesRepository.findAll();
        if (profiles.size() == 0) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(mapper.toDictionaryProfileResponse(profiles), httpStatus);
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<DictionaryProfileResponse> getProfile(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.OK;
        DictionaryProfileResponse response = mapper.toDictionaryProfileResponse(
                profilesRepository.findById(id).orElse(null)
        );
        if (response == null) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @GetMapping("/profiles/{profileId}/dictionaries/{dictionaryId}")
    public ResponseEntity<DictionaryProfileResponse.Dictionary> getProfileDictionary(
            @PathVariable String profileId,
            @PathVariable String dictionaryId) {
        HttpStatus httpStatus = HttpStatus.OK;
        DictionaryProfileResponse.Dictionary response = null;
        PersistentDictionaryProfile profile = profilesRepository.findByProfileAndDictionary(profileId, dictionaryId);
        if (profile != null && profile.getDictionaries().size() > 0) {
            response = mapper.toDictionaryProfileResponse(profile).getDictionaries().get(0);
        } else {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(response, httpStatus);

    }

    @GetMapping("/words")
    public ResponseEntity<Page<PersistentWord>> getProfileDictionaryWords(
            @RequestParam String search,
            @RequestParam(name = "page") Integer pageNumber,
            @RequestParam Integer pageSize,
            @RequestParam(name = "profile") String profileName,
            @RequestParam String dictionary) {
        HttpStatus httpStatus = HttpStatus.OK;
        Sort sort = Sort.by("word");
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        Page<PersistentWord> page = wordsRepository.findAllByWord(search, profileName, dictionary, pageRequest);
        return new ResponseEntity<>(page, httpStatus);
    }

    @PostMapping("/profiles")
    public ResponseEntity<DictionaryProfileResponse> createProfile(
            @Valid @RequestBody DictionaryProfilePayload profilePayload) {
        HttpStatus httpStatus = HttpStatus.OK;
        DictionaryProfileResponse response = null;
        if (profilesRepository.findByName(profilePayload.getName()) == null) {
            response = mapper.toDictionaryProfileResponse(
                    profilesRepository.insert(
                            mapper.toPersistentDictionaryProfile(profilePayload)
                    )
            );
            httpStatus = HttpStatus.CREATED;
        } else {
            httpStatus = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping("/profiles/{profileId}/{dictionaryId}/analyze")
    public ResponseEntity<String> analyze(
            @PathVariable String profileId,
            @PathVariable String dictionaryId,
            @RequestParam(defaultValue="false") boolean useCache,
            @RequestBody JsonNode body) throws JsonProcessingException {
        HttpStatus httpStatus = HttpStatus.OK;
        String response = null;
        AnalyzePayload payload = objectMapper.treeToValue(body, AnalyzePayload.class);
        if (useCache) {

        } else {
            try {
                PersistentAnalysis analysis = analyzeService.analyze(profileId, dictionaryId, payload.getText());
                response = objectMapper.writeValueAsString(sentimentRepository.save(analysis));
            } catch (DictionaryNotFoundException e) {
                httpStatus = HttpStatus.NOT_FOUND;
                response = e.getMessage();
            }
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping("/profiles/{profileId}/dictionaries")
    public ResponseEntity<PersistentDictionary> createDictionaryForProfile(
            @PathVariable String profileId,
            @Valid @RequestBody ProfileDictionaryPayload dictionaryPayload) {
        HttpStatus httpStatus = HttpStatus.OK;
        PersistentDictionary response = null;
        PersistentDictionaryProfile profile = profilesRepository.findById(profileId).orElse(null);
        if (profile == null) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else {
            PersistentDictionary dictionary = mapper.toPersistentDictionary(dictionaryPayload);
            boolean isDefault = profile.getDictionaries().size() == 0;
            if (isDefault) {
                profile.setActiveDictionary(dictionary.getId());
            } else {
                profile.getDictionaries().removeIf(d -> d.getName().equals(dictionaryPayload.getName()));
            }
            profile.getDictionaries().add(dictionary);
            // Update words language
            List<PersistentWord> words = wordsRepository
                    .findByProfileAndDictionary(profile.getName(), dictionaryPayload.getName());
            for(PersistentWord word : words) {
                word.setLanguage(dictionaryPayload.getLanguage());
            }
            wordsRepository.saveAll(words);
            profilesRepository.save(profile);
            response = dictionary;
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping("/words")
    public ResponseEntity<PersistentWord> populateDictionaryForProfile(
            @Valid @RequestBody JsonNode jsonNode) throws JsonProcessingException {
        HttpStatus httpStatus = HttpStatus.OK;
        PersistentWord response = null;
        PersistentWord wordPayload = objectMapper.treeToValue(jsonNode, PersistentWord.class);
        PersistentDictionaryProfile profile = profilesRepository
                .findByProfileAndDictionary(wordPayload.getProfile(), wordPayload.getDictionary());
        if (profile == null || profile.getDictionaries().size() == 0) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else {
            wordPayload.setLanguage(profile.getLanguage());
            response = wordsRepository.save(wordPayload);
            cache.updateRules(wordPayload.getDictionary());
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @PutMapping("/profiles/{profileId}")
    public ResponseEntity<DictionaryProfileResponse> updateProfile(
            @PathVariable String profileId,
            @RequestBody UpdateProfilePayload delta) {
        HttpStatus httpStatus = HttpStatus.OK;
        DictionaryProfileResponse response = null;
        PersistentDictionaryProfile profile = profilesRepository.findById(profileId).orElse(null);
        if (profile == null) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else {
            if (delta.getActiveDictionary() != null) {
                profile.setActiveDictionary(delta.getActiveDictionary());
            }
            if (delta.getDescription() != null) {
                profile.setDescription(delta.getDescription());
            }
            if (delta.getLanguage() != null) {
                if (new IsSupportedLanguage.Validator().isValid(delta.getLanguage(), null)) {
                    profile.setLanguage(delta.getLanguage());
                }
            }
            response = mapper.toDictionaryProfileResponse(profilesRepository.save(profile));
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @PutMapping("/words/{id}")
    public ResponseEntity<PersistentWord> updateWord(
            @PathVariable String id,
            @RequestBody JsonNode word) {
        HttpStatus httpStatus = HttpStatus.OK;
        PersistentWord response = wordsRepository.findById(id).orElse(null);
        if (response == null) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else {
            try {
                response = objectMapper.readerForUpdating(response).readValue(word);
                wordsRepository.save(response);
//                cache.updateRules(response.getDictionary());
                cache.onWordUpdate(response);
            } catch (IOException e) {
                e.printStackTrace();
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @DeleteMapping("/words/{id}")
    public ResponseEntity<PersistentWord> deleteWord(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.OK;
        PersistentWord response = wordsRepository.findById(id).orElse(null);
        if (response == null) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else {
            wordsRepository.deleteById(id);
        }
        return new ResponseEntity<>(response, httpStatus);
    }


    @PostMapping("/profiles/testme")
    public ResponseEntity<String> postTest(@RequestBody @Valid DictionaryProfileResponse profile) {

        return new ResponseEntity<>("", HttpStatus.OK);
    }

}

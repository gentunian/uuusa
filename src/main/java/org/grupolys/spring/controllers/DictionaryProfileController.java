package org.grupolys.spring.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupolys.hazelcast.HazelcastCache;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.profiles.exception.DictionaryNotFoundException;
import org.grupolys.spring.model.mappers.DictionaryProfileMapper;
import org.grupolys.spring.model.mappers.WordMapper;
import org.grupolys.spring.model.payloads.*;
import org.grupolys.spring.model.persistence.*;
import org.grupolys.spring.model.responses.DictionaryProfileResponse;
import org.grupolys.spring.model.responses.ErrorResponse;
import org.grupolys.spring.repositories.ProfilesRepository;
import org.grupolys.spring.repositories.SentimentRepository;
import org.grupolys.spring.repositories.Words2Repository;
import org.grupolys.spring.service.AnalyzeService;
import org.grupolys.spring.validations.IsSupportedLanguage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
public class DictionaryProfileController {
    @Autowired
    private WordMapper wordMapper;

    @Autowired
    private Words2Repository words2Repository;

    @Autowired
    private HazelcastCache cache;

    @Autowired
    private DictionaryProfileMapper mapper;

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
    public ResponseEntity<Page<PersistentWord2>> getProfileDictionaryWords(
            @RequestParam(defaultValue = ".*") String search,
            @RequestParam(defaultValue = "0", name = "page") Integer pageNumber,
            @RequestParam(defaultValue = "25") Integer pageSize,
            @RequestParam(required = false) PartOfSpeech[] partOfSpeech,
            @RequestParam(required = false) String dictionary) {
        HttpStatus httpStatus = HttpStatus.OK;
        Sort sort = Sort.by("word");
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        Page<PersistentWord2> page = words2Repository
                .findAllBySearch(search, dictionary, partOfSpeech, pageRequest);
        return new ResponseEntity<>(page, httpStatus);
    }

    @GetMapping("/words/{id}")
    public ResponseEntity<PersistentWord2> getWord(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.OK;
        PersistentWord2 word = words2Repository.findById(id).orElse(null);
        if (word == null) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(word, httpStatus);
    }

    @PostMapping("/profiles")
    public ResponseEntity<DictionaryProfileResponse> createProfile(
            @Valid @RequestBody DictionaryProfilePayload profilePayload) {
        HttpStatus httpStatus;
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
            @RequestParam(defaultValue = "text") String field,
            @RequestParam(defaultValue = "false") boolean useCache,
            @RequestBody JsonNode body) throws JsonProcessingException {
        HttpStatus httpStatus = HttpStatus.OK;
        String response = null;
//        AnalyzePayload payload = objectMapper.treeToValue(body, AnalyzePayload.class);
        if (useCache) {

        } else {
            try {
                PersistentAnalysis analysis = analyzeService.analyze(profileId, dictionaryId, body.get(field).asText());
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
        } else if (profile.getDictionaries().stream()
                .filter(d -> d.getName().equals(dictionaryPayload.getName())).findAny().orElse(null) != null) {
            httpStatus = HttpStatus.CONFLICT;
        } else {
            PersistentDictionary dictionary = mapper.toPersistentDictionary(dictionaryPayload);
            // if there's no other dict, set this dictionary as the active directory
            if (profile.getDictionaries().size() == 0) {
                profile.setActiveDictionary(dictionary.getId());
            }
            profile.getDictionaries().add(dictionary);
            profilesRepository.save(profile);
            cache.onDictionaryCreated2(dictionary.getId());
            response = dictionary;
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping("/words")
    public ResponseEntity<JsonNode> postWord(
            @Valid @RequestBody PostWordPayload payload) {
        HttpStatus httpStatus = HttpStatus.OK;
        JsonNode response;
        PersistentDictionaryProfile profile = profilesRepository
                .findByProfileAndDictionary(payload.getProfile(), payload.getDictionary());
        PersistentWord2 existingWord = words2Repository.findByWord(payload.getWord());
        // Check for profile with dictionary existence
        if (profile == null) {
            httpStatus = HttpStatus.NOT_FOUND;
            response = objectMapper.valueToTree(
                    new ErrorResponse("Profile not found.")
            );
        } else if (profile.getDictionaries().size() == 0) {
            httpStatus = HttpStatus.BAD_REQUEST;
            response = objectMapper.valueToTree(
                    new ErrorResponse("Profile does not have any dictionaries yet.")
            );
        } else if (existingWord != null) {
            httpStatus = HttpStatus.CONFLICT;
            response = objectMapper.valueToTree(
                    new ErrorResponse("Word already exists.", "/words/" + existingWord.getId())
            );
        } else {
            PersistentWord2 word = wordMapper.toPersistentWord2(payload);
            word = words2Repository.save(word);
            cache.onWordAdded2(word);
            response = objectMapper.valueToTree(word);
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
    public ResponseEntity<JsonNode> putWord(
            @PathVariable String id,
            @RequestBody @Valid PersistentWord2 payload) {
        HttpStatus httpStatus = HttpStatus.OK;
        JsonNode response = null;
        PersistentWord2 word = words2Repository.findById(id).orElse(null);
        if (word == null) {
            httpStatus = HttpStatus.NOT_FOUND;
            response = objectMapper.valueToTree(new ErrorResponse("Word not found"));
        } else if (!word.getDictionary().equals(payload.getDictionary())) {
            httpStatus = HttpStatus.BAD_REQUEST;
            response = objectMapper.valueToTree(
                    new ErrorResponse("You can't move word to another dictionary. " +
                            "Word belongs to dictionary '" + word.getDictionary() + "'")
            );
        } else if (!word.getProfile().equals(payload.getProfile())) {
            httpStatus = HttpStatus.BAD_REQUEST;
            response = objectMapper.valueToTree(
                    new ErrorResponse("You can't move word to another profile. " +
                            "Word belongs to profile '" + word.getProfile() + "'")
            );
        } else if (!word.getLanguage().equals(payload.getLanguage())) {
            httpStatus = HttpStatus.BAD_REQUEST;
            response = objectMapper.valueToTree(
                    new ErrorResponse("You can't change word language. This is managed by the dictionary")
            );
        } else if (!word.getWord().equals(payload.getWord())) {
            httpStatus = HttpStatus.BAD_REQUEST;
            response = objectMapper.valueToTree(new ErrorResponse("You can't change the word. " +
                    "Word '" + payload.getWord() + "' is not valid."));
        } else {
            if (!words2Repository.findAllWordsByLemmas(payload.getDictionary(), payload.getPartOfSpeech()).isEmpty()) {
                httpStatus = HttpStatus.BAD_REQUEST;
                response = objectMapper.valueToTree(
                        new ErrorResponse("Provided lemma(s) doesn't exist in dictionary: '" +
                                payload.getDictionary() + "'.")
                );
            } else {
                payload.setId(word.getId());
                payload.setLanguage(word.getLanguage());
                response = objectMapper.valueToTree(words2Repository.save(payload));
                cache.onWordUpdated2(payload);
            }
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @PatchMapping("/words/{id}")
    public ResponseEntity<JsonNode> patchWord(
            @PathVariable String id,
            @Valid @RequestBody PatchWordPayload payload) {
        HttpStatus httpStatus = HttpStatus.OK;
        JsonNode response = null;
        PersistentWord2 word = words2Repository.findById(id).orElse(null);
        if (word == null) {
            httpStatus = HttpStatus.NOT_FOUND;
            response = objectMapper.valueToTree(
                    new ErrorResponse("Word not found.")
            );
        } else {
            word = wordMapper.toPersistentWord2(word, payload);
            response = objectMapper.valueToTree(word);
            cache.onWordUpdated2(words2Repository.save(word));
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        e.printStackTrace();
    }

    @PostMapping("/profiles/testme")
    public ResponseEntity<String> postTest(@RequestBody @Valid DictionaryProfileResponse profile) {

        return new ResponseEntity<>("", HttpStatus.OK);
    }

}

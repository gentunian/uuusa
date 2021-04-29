package org.grupolys.spring.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.profiles.exception.NoRulesLoadedForDictionaryException;
import org.grupolys.spring.model.mappers.DictionaryProfileMapper;
import org.grupolys.spring.model.payloads.DictionaryProfilePayload;
import org.grupolys.spring.model.payloads.PostWordPayload;
import org.grupolys.spring.model.payloads.ProfileDictionaryPayload;
import org.grupolys.spring.model.persistence.PersistentAnalysis;
import org.grupolys.spring.model.persistence.PersistentDictionary;
import org.grupolys.spring.model.persistence.PersistentDictionaryProfile;
import org.grupolys.spring.model.persistence.PersistentWord;
import org.grupolys.spring.model.responses.DictionaryProfileResponse;
import org.grupolys.spring.model.responses.ErrorResponse;
import org.grupolys.spring.service.AnalyzeService;
import org.grupolys.spring.service.ProfilesService;
import org.grupolys.spring.service.WordsService;
import org.grupolys.spring.service.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.json.Json;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v2")
@Validated
public class DictionaryProfileController {
    @Autowired
    private DictionaryProfileMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalyzeService analyzeService;

    @Autowired
    private ProfilesService profilesService;

    @Autowired
    private WordsService wordsService;

    @GetMapping("/profiles")
    public ResponseEntity<List<DictionaryProfileResponse>> getProfiles() {
        HttpStatus httpStatus = HttpStatus.OK;
        List<PersistentDictionaryProfile> profiles = profilesService.getProfiles();
        return new ResponseEntity<>(mapper.toDictionaryProfileResponse(profiles), httpStatus);
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<DictionaryProfileResponse> getProfile(@PathVariable String id) {
        HttpStatus httpStatus;
        DictionaryProfileResponse response = null;
        try {
            PersistentDictionaryProfile profile = profilesService.getProfile(id);
            response = mapper.toDictionaryProfileResponse(profile);
            httpStatus = HttpStatus.OK;
        } catch (ServiceException e) {
            e.printStackTrace();
            httpStatus = e.getErrorResponse().getCode();
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @GetMapping("/profiles/{profileId}/dictionaries/{dictionaryId}")
    public ResponseEntity<DictionaryProfileResponse.Dictionary> getProfileDictionary(
            @PathVariable String profileId,
            @PathVariable String dictionaryId) {
        HttpStatus httpStatus;
        DictionaryProfileResponse.Dictionary response = null;
        PersistentDictionaryProfile profile = null;
        try {
            profile = profilesService.getProfile(profileId, dictionaryId);
            response = mapper.toDictionaryProfileResponse(profile).getDictionaries().get(0);
            httpStatus = HttpStatus.OK;
        } catch (ServiceException e) {
            e.printStackTrace();
            httpStatus = e.getErrorResponse().getCode();
        }

        return new ResponseEntity<>(response, httpStatus);

    }

    @GetMapping("/words")
    public ResponseEntity<Page<PersistentWord>> getProfileDictionaryWords(
            @RequestParam(defaultValue = ".*") String search,
            @RequestParam(defaultValue = "0", name = "page") Integer pageNumber,
            @RequestParam(defaultValue = "25") Integer pageSize,
            @RequestParam(required = false) PartOfSpeech[] partOfSpeech,
            @RequestParam(required = false) String dictionary) {
        HttpStatus httpStatus = HttpStatus.OK;
        Page<PersistentWord> page = wordsService.searchForWord(search, dictionary, partOfSpeech, pageNumber, pageSize);
        return new ResponseEntity<>(page, httpStatus);
    }

    @GetMapping("/words/{id}")
    public ResponseEntity<PersistentWord> getWord(@PathVariable String id) {
        HttpStatus httpStatus;
        PersistentWord word = null;
        try {
            word = wordsService.getWordById(id);
            httpStatus = HttpStatus.OK;
        } catch (ServiceException e) {
            e.printStackTrace();
            httpStatus = e.getErrorResponse().getCode();
        }
        return new ResponseEntity<>(word, httpStatus);
    }

    @PostMapping("/profiles")
    public ResponseEntity<DictionaryProfileResponse> createProfile(
            @Valid @RequestBody DictionaryProfilePayload profilePayload) {
        HttpStatus httpStatus;
        DictionaryProfileResponse response = null;

        try {
            response = profilesService.saveProfile(profilePayload);
            httpStatus = HttpStatus.CREATED;
        } catch (ServiceException e) {
            e.printStackTrace();
            httpStatus = e.getErrorResponse().getCode();
        }

        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping("/profiles/rd/{dictionaryName}/analyze")
    public ResponseEntity<String> rdAnalyze(
            @PathVariable String dictionaryName,
            @RequestParam(defaultValue = "text") String field,
            @RequestParam(defaultValue = "false") boolean useCache,
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestBody JsonNode body) throws JsonProcessingException {
        HttpStatus httpStatus = HttpStatus.OK;
        String response = null;
        String text = body.get(field).asText();
        String profileName = "Reputación Digital";

        PersistentDictionaryProfile profile = profilesService.getProfileByName(profileName);
        if (profile == null) {
            httpStatus = HttpStatus.NOT_FOUND;
            response = objectMapper.writeValueAsString(
                    new ErrorResponse(profileName + "Profile not found", HttpStatus.NOT_FOUND)
            );
        } else {
            PersistentDictionary dictionary = profile.getDictionaries().stream()
                    .filter(d -> d.getName().equals(dictionaryName)).findFirst().orElse(null);
            if (dictionary == null) {
                httpStatus = HttpStatus.NOT_FOUND;
                response = objectMapper.writeValueAsString(
                        new ErrorResponse("No dictionary found named '" + dictionaryName +
                                "' for profile '" + profileName + "'", HttpStatus.NOT_FOUND)
                );
            } else {
                String dictionaryId = dictionary.getId();
                String profileId = profile.getId();
                if (useCache) {
                    try {
                        response = objectMapper.writeValueAsString(analyzeService.getAnalysis(text, dictionaryId));
                        httpStatus = HttpStatus.FOUND;
                    } catch (ServiceException e) {
                        e.printStackTrace();
                        httpStatus = HttpStatus.NOT_FOUND;
                    }
                } else {
                    try {
                        PersistentAnalysis analysis = analyzeService.analyze(profileId, dictionaryId, text, dryRun);
                        response = objectMapper.writeValueAsString(analysis);
                    } catch (NoRulesLoadedForDictionaryException e) {
                        httpStatus = HttpStatus.NOT_FOUND;
                        response = e.getMessage();
                    }
                }
            }
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping("/profiles/{profileId}/{dictionaryId}/analyze")
    public ResponseEntity<String> analyze(
            @PathVariable String profileId,
            @PathVariable String dictionaryId,
            @RequestParam(defaultValue = "text") String field,
            @RequestParam(defaultValue = "false") boolean useCache,
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestBody JsonNode body) throws JsonProcessingException {
        HttpStatus httpStatus = HttpStatus.OK;
        String response = null;
        String text = body.get(field).asText();
        if (useCache) {
            try {
                response = objectMapper.writeValueAsString(analyzeService.getAnalysis(text, dictionaryId));
                httpStatus = HttpStatus.FOUND;
            } catch (ServiceException e) {
                e.printStackTrace();
                httpStatus = HttpStatus.NOT_FOUND;
            }
        } else {
            try {
                PersistentAnalysis analysis = analyzeService.analyze(profileId, dictionaryId, text, dryRun);
                response = objectMapper.writeValueAsString(analysis);
            } catch (NoRulesLoadedForDictionaryException e) {
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
        HttpStatus httpStatus;
        PersistentDictionary response = null;

        try {
            response = profilesService.createDictionaryForProfile(profileId, dictionaryPayload);
            httpStatus = HttpStatus.OK;
        } catch (ServiceException e) {
            e.printStackTrace();
            httpStatus = e.getErrorResponse().getCode();
        }

        return new ResponseEntity<>(response, httpStatus);
    }


    @PostMapping("/words")
    public ResponseEntity<JsonNode> postWord(
            @RequestBody @Valid PostWordPayload payload) {
        HttpStatus httpStatus;
        JsonNode response;
        try {
            response = objectMapper.valueToTree(wordsService.addWord(payload, true));
            httpStatus = HttpStatus.OK;
        } catch (ServiceException e) {
            e.printStackTrace();
            httpStatus = e.getErrorResponse().getCode();
            response = objectMapper.valueToTree(e.getErrorResponse());
        }

        return new ResponseEntity<>(response, httpStatus);
    }

    @PutMapping("/words/{id}")
    public ResponseEntity<JsonNode> putWord(
            @PathVariable String id,
            @RequestBody @Valid PersistentWord payload) {
        HttpStatus httpStatus;
        JsonNode response = null;

        try {
            response = objectMapper.valueToTree(wordsService.editWord(id, payload, true));
            httpStatus = HttpStatus.OK;
        } catch (ServiceException e) {
            e.printStackTrace();
            httpStatus = e.getErrorResponse().getCode();
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

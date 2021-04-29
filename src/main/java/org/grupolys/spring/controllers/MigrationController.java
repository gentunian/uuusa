package org.grupolys.spring.controllers;

import org.grupolys.dictionary.DefaultWordType;
import org.grupolys.dictionary.WordType;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.spring.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v2")
@Validated
public class MigrationController {

    private final MigrationService migrationService;

    @Autowired
    MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/migration/{type}/{profileId}/{dictionaryId}")
    public ResponseEntity<String> migrateKeyValueWords(
            HttpServletRequest httpRequest,
            @PathVariable String type,
            @PathVariable String profileId,
            @PathVariable String dictionaryId,
            @RequestParam MultipartFile file) throws IOException {
        HttpStatus httpStatus = HttpStatus.OK;
        if (!file.isEmpty()) {
            InputStreamReader input = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            if (type.toLowerCase().equals("lemmas")) {
                // process lemmas.
                migrationService.migrateLemmas(profileId, dictionaryId, input);

            } else if (type.toLowerCase().equals("negating")) {
                // process negating words.
                migrationService.migrateNegation(profileId, dictionaryId, input);

            } else {
                // process anything from the lookup tables.
                WordType wordTypeValue;
                PartOfSpeech partOfSpeech = PartOfSpeech.getPartOfSpeech(type);
                if (partOfSpeech.equals(PartOfSpeech.NOPOSTAG)) {
                    wordTypeValue = DefaultWordType.getDefaultWordType(type);
                } else {
                    wordTypeValue = partOfSpeech;
                }
                migrationService.migrateLookupTablesByPartOfSpeech(profileId, dictionaryId, wordTypeValue, input);
            }
        }
        return new ResponseEntity<>("", httpStatus);
    }
}

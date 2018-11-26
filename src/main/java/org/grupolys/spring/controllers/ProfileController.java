package org.grupolys.spring.controllers;

import java.util.Map;

import javax.validation.Valid;

import org.grupolys.profiles.FilesystemProfileCreator;
import org.grupolys.profiles.PartOfSpeech;
import org.grupolys.profiles.Profile;
import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.grupolys.spring.dao.utils.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    @PutMapping("/profiles/{name}")
    public ResponseEntity<Profile> putProfile(@PathVariable String name, @RequestBody @Valid Profile newProfile) {
        HttpStatus httpStatus = HttpStatus.OK;
        FilesystemProfileCreator fpc = new FilesystemProfileCreator();
        Profile profile = null;

        try {
            profile = fpc.loadProfile(name);
        } catch (ProfileNotFoundException e) {
            profile = new Profile();
        }

        profile.mergeProfile(newProfile);
        fpc.saveProfile(name, profile);
        return new ResponseEntity<Profile>(newProfile, httpStatus);
    }

    @PatchMapping("/profiles/{name}")
    public ResponseEntity<Profile> patchProfile(@PathVariable String name, @RequestBody Profile profile) {
        HttpStatus httpStatus = HttpStatus.OK;
        return new ResponseEntity<Profile>(profile, httpStatus);
    }

    @PutMapping("/profiles/{name}/{pos}/{word}/{weight}")
    public ResponseEntity<String> putProfilePos(@PathVariable String name, @PathVariable String pos,
            @PathVariable String word, @PathVariable Float weight) {
        HttpStatus httpStatus = HttpStatus.OK;
        FilesystemProfileCreator fpc = new FilesystemProfileCreator();
        Profile profile = null;

        try {
            profile = fpc.loadProfile(name);
        } catch (ProfileNotFoundException e) {
            profile = new Profile();
        }

        profile.addEmotion(pos, word.toLowerCase(), weight);
        fpc.saveProfile(name, profile);
        Utils.updateRules(name);
        String response = "OK";
        return new ResponseEntity<String>(response, httpStatus);
    }

    @PostMapping("/profiles/{name}/{pos}")
    public ResponseEntity<String> postProfilePos(@PathVariable String name, @PathVariable String pos,
            @RequestBody @Valid Map<String, Float> body) {

        HttpStatus httpStatus = HttpStatus.OK;
        String response = "";
        FilesystemProfileCreator fpc = new FilesystemProfileCreator();
        Profile profile = null;

        try {
            profile = fpc.loadProfile(name);
        } catch (ProfileNotFoundException e) {
            profile = new Profile();
        }

        for (String word : body.keySet()) {
            profile.addEmotion(pos, word.toLowerCase(), body.get(word));
        }

        fpc.saveProfile(name, profile);
        Utils.updateRules(name);
        return new ResponseEntity<String>(response, httpStatus);
    }

    @GetMapping("/test/{alias}")
    public ResponseEntity<String> test(@PathVariable String alias) {
        String test = PartOfSpeech.getPartOfSpeech(alias);
        return new ResponseEntity<String>(test, HttpStatus.OK);
    }
}

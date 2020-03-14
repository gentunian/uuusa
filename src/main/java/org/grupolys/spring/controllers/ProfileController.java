package org.grupolys.spring.controllers;

import javax.validation.Valid;

import org.grupolys.hazelcast.HazelcastCache;
import org.grupolys.profiles.*;
import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.grupolys.spring.controllers.exception.InvalidPosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Deprecated
@RestController
public class ProfileController {

    @Autowired
    private HazelcastCache cache;
    @Autowired
    private FilesystemProfileCreator fpc;

    @GetMapping("/profiles")
    public ResponseEntity<String[]> getProfiles() {
        HttpStatus httpStatus = HttpStatus.OK;
        String[] profiles = fpc.profiles();

        return new ResponseEntity<String[]>(profiles, httpStatus);
    }

    @PutMapping("/profiles/{name}")
    public ResponseEntity<Profile> putProfile(@PathVariable String name, @RequestBody @Valid Profile newProfile) {
        HttpStatus httpStatus = HttpStatus.OK;
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

    @PostMapping("/profiles/{name}/words/{pos}")
    public ResponseEntity<String> postProfilePos(@PathVariable String name, @PathVariable String pos,
            @RequestBody @Valid Map<String, Object> body) {

        HttpStatus httpStatus = HttpStatus.OK;
        String response = "";
        Profile profile = null;

        try {
            profile = (Profile) fpc.loadProfile(name);
        } catch (ProfileNotFoundException e) {
            profile = new Profile();
        }

        try {
            PartOfSpeech alias = PartOfSpeech.getPartOfSpeech(pos);
            if (!pos.equals("negating")) {
                if (alias != null) {
                    for (String word : body.keySet()) {
                        Object weight = body.get(word);
                        if (weight instanceof Integer) {
                            weight = ((Integer) body.get(word)).floatValue();
                        }
                        profile.addEmotion(alias.name(), word.toLowerCase(), (Float) weight);
                    }
                } else if (pos.equals("booster")) {
                    for (String word : body.keySet()) {
                        Object weight = body.get(word);
                        if (weight instanceof Integer) {
                            weight = ((Integer) body.get(word)).floatValue();
                        }
                        profile.addBoosterWord(word.toLowerCase(), (Float) weight);
                    }
                } else if (pos.equals("emoticon")) {
                    for (String word : body.keySet()) {
                        Object weight = body.get(word);
                        if (weight instanceof Integer) {
                            weight = ((Integer) body.get(word)).floatValue();
                        }
                        profile.addEmoticon(word.toLowerCase(), (Float) weight);
                    }
                } else {
                    throw new InvalidPosException("Invalid POStag: " + pos);
                }
            } else {
                profile.addNegatingWord((String) body.get("word"));
            }
            response = "{\"message\": \"OK\"}";
        } catch (InvalidPosException e) {
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            response = "{\"error\": \"Invalid postag: " + pos +  "\"}";
        }

        fpc.saveProfile(name, profile);
//        cache.updateRules(name);
        
        return new ResponseEntity<String>(response, httpStatus);
    }

    @DeleteMapping("/profiles/{name}/words/{pos}/{word}")
    public ResponseEntity<String> removeWord(@PathVariable String name, @PathVariable String pos,
            @PathVariable String word) {
        HttpStatus httpStatus = HttpStatus.OK;
        String response = "{\"message\": \"OK\"}";
        Profile profile = null;

        try {
            profile = (Profile) fpc.loadProfile(name);
            PartOfSpeech alias = PartOfSpeech.getPartOfSpeech(pos);
            if (!pos.equals("negating")) {
                if (alias != null) {
                    profile.removeEmotion(alias.name(), word);
                } else if (pos.equals("booster")) {
                    profile.removeBoosterWord(word);
                } else if (pos.equals("emoticon")) {
                    profile.removeEmoticon(word);
                } else {
                    throw new InvalidPosException("Invalid POStag: " + pos);
                }
            } else {
                profile.removeNegatingWord(word);
            }
        } catch (ProfileNotFoundException e) {
            httpStatus = HttpStatus.NOT_FOUND;
            response = "{ \"message\": \"Profile " + name + " not found.\"";
        } catch (InvalidPosException e) {
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            response = "{\"error\": \"Invalid postag: " + pos + "\"}";
        }
        fpc.saveProfile(name, profile);
//        cache.updateRules(name);
        return new ResponseEntity<String>(response, httpStatus);
    }

    @GetMapping("/profiles/{name}/words/{pos}")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getWordsByPostag(@PathVariable String name,
            @PathVariable String pos) {
        HttpStatus httpStatus = HttpStatus.OK;
        Map<String, List<Map<String, Object>>> response = new HashMap<String, List<Map<String, Object>>>();
        Profile profile = null;

        try {
            profile = (Profile) fpc.loadProfile(name);
            PartOfSpeech alias = PartOfSpeech.getPartOfSpeech(pos);
            List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();

            // the only type of word not having weight are negating words
            if (!pos.equals("negating")) {
                Map<String, Float> values = null;
                if (alias != null) {
                    values = profile.getEmotions().get(alias.name());
                } else if (pos.equals("booster")) {
                    values = profile.getBoosters();
                } else if (pos.equals("emoticon")) {
                    values = profile.getEmoticons();
                } else {
                    throw new InvalidPosException("Invalid POStag: " + pos);
                }

                if (values != null) {
                    // we process the Map<String, Float>
                    for (String word : values.keySet()) {
                        Float weight = values.get(word);
                        Map<String, Object> m = new HashMap<String, Object>();
                        m.put("weight", weight);
                        m.put("word", word);
                        map.add(m);
                    }
                }
            } else {
                map = profile.getNegating().stream().map(p -> {
                    Map<String, Object> m = new HashMap<String, Object>();
                    m.put("word", p);
                    return m;
                }).collect(Collectors.toList());
            }
            response.put("data", map);
        } catch (ProfileNotFoundException e) {
            httpStatus = HttpStatus.NOT_FOUND;
            // response = "Profile " + name + " not found.";
        } catch (InvalidPosException e) {
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        }

        return new ResponseEntity<Map<String, List<Map<String, Object>>>>(response, httpStatus);
    }

    @GetMapping("/test/{alias}")
    public ResponseEntity<String> test(@PathVariable String alias) {
        String test = "";//PartOfSpeech.getPartOfSpeech(alias);
        return new ResponseEntity<String>(test, HttpStatus.OK);
    }
}

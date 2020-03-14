package org.grupolys.spring.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.grupolys.profiles.exception.ProfileNotFoundException;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.processor.Processor;
import org.grupolys.samulan.util.SentimentDependencyGraph;
import org.grupolys.samulan.util.SentimentInformation;
import org.grupolys.spring.controllers.exception.NoTextToProcessException;
import org.grupolys.spring.service.SamulanProcessorService;
import org.grupolys.spring.service.SamulanRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

/**
 * AnalizeController
 */
@RestController
public class AnalizeController {
    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private SamulanProcessorService processorService;
    @Autowired
    private SamulanRulesService rulesService;

    
    private ObjectId saveMap(Map<String, Object> map, String collectionName, String db) {
        Document document = new Document(map);

        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        // creates an index for the hashed text in order to query faster
        collection.createIndex(Indexes.hashed("text"));
        collection.insertOne(document);

        return (ObjectId) document.get("_id");
    }

    private String getMapById(String id, String collectionName, String db) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document doc = null;

        try {
            doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException e) {
            System.out.println("Exception managed.");
            e.printStackTrace();
        }

        return doc == null ? "" : doc.toJson();
    }

    private Map<String, Object> getMapByText(String text, String collectionName, String db) {
        MongoDatabase database = this.mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document doc = collection.find(Filters.eq("text", text)).first();

        return (Map<String, Object>) doc;
    }

    public class SamulanJsonInput implements Iterable<Map<String, Object>> {
        private List<Map<String, Object>> parsedJson;

        public SamulanJsonInput(String jsonString) throws MalformedParametersException {
            ObjectMapper mapper = new ObjectMapper();
            try {
                // TODO: fix this workaround.
                if (jsonString.indexOf("[") != 0) {
                    jsonString = "[" + jsonString + "]";
                }
                this.parsedJson = mapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
                throw new MalformedParametersException("JSON String could not be parsed.");
            }
        }

        @Override
        public Iterator<Map<String, Object>> iterator() {
            return this.parsedJson.iterator();
        }

        @Override
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this.parsedJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    @GetMapping("/profiles/{profileName}/analysis/{id}")
    public ResponseEntity<String> getAnalysis(@PathVariable String id, @PathVariable String profileName) {
        HttpStatus httpStatus = HttpStatus.OK;
        String response = this.getMapById(id, "sentiment", profileName);
        if (response.length() == 0) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<String>(response, httpStatus);
    }

    @PostMapping("/profiles/{profileName}/analyze")
    public ResponseEntity<String> analyze(@RequestHeader("Content-Type") String contentType,
            @PathVariable String profileName, @RequestParam(defaultValue = "text") String field,
            @RequestParam(required=false, defaultValue="false") boolean useCache,
            @RequestBody String body) {
        HttpStatus httpStatus = HttpStatus.OK;
        String response = "";

        System.out.println(body);

        try {
            if (!contentType.equals("application/json")) {
                throw new UnsupportedMediaTypeStatusException("Only application/json content-type is supported");
            }

            SamulanJsonInput input = new SamulanJsonInput(body);

            for (Map<String, Object> obj : input) {
                String text = (String) obj.get(field);
                System.out.println("field '" + field + "' has text: '" + text + "'");

                if (text == null || text.length() == 0) {
                    throw new NoTextToProcessException("Field " + field + " has no text to analyze");
                } else {
                    String analysisId = null;

                    Map<String, Object> doc = this.getMapByText(text, "sentiment", profileName);
                    if (doc != null && useCache) {
                        System.out.println("Fetching already stored data");
                        analysisId = doc.get("_id").toString();
                        obj.put("_cached", true);
                    } else {
                        RuleBasedAnalyser rba = rulesService.getRules().get(profileName);
                        if (rba == null) {
                            throw new ProfileNotFoundException("Rules for profile " + profileName + " not found");
                        }
                        Processor processor = processorService.getProcessor();
                        doc = analyse(text, processor, rba);
                        analysisId = this.saveMap(doc, "sentiment", profileName).toString();
                    }
                    obj.put("_sentiment", doc.get("sentiment").toString());
                    obj.put("_sentimentWeight", doc.get("sentimentWeight").toString());
                    obj.put("_analysisId", analysisId);
                    System.out.println(obj.toString());
                }
            }
            response = input.toString();
        } catch (ProfileNotFoundException e) {
            System.out.println(e.getMessage());
            httpStatus = HttpStatus.NOT_FOUND;
            response = "Profile " + profileName + " not found.";
        } catch (MalformedParametersException e) {
            e.printStackTrace();
            httpStatus = HttpStatus.BAD_REQUEST;
        // } catch (FileNotFoundException e) {
        //     e.printStackTrace();
        //     httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (NoTextToProcessException e) {
            System.out.println(e.getMessage());
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
        }

        return new ResponseEntity<String>(response, httpStatus);
    }

    private Map<String, Object> analyse(String input, Processor processor, RuleBasedAnalyser rba) {
        List<SentimentDependencyGraph> sdgs = processor.process(input);
        List<SentimentInformation> sis = sdgs.stream()
                .map((SentimentDependencyGraph dg) -> (rba.analyse(dg, (short) 0))).collect(Collectors.toList());

        SentimentInformation si = rba.merge(sis);
        String sentiment = "";

        // Taken from Samulan.analyse method, please double check why not pos >= neg
        if (si.getPositiveSentiment() > si.getNegativeSentiment()
                || (si.getPositiveSentiment() == si.getNegativeSentiment() && si.getPositiveSentiment() > 0)) {
            sentiment = "1";
        } else if (si.getSemanticOrientation() < 0) {
            sentiment = "-1";
        } else {
            sentiment = "0";
        }

        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> sentiAnalysisTree = new ArrayList<Map<String, Object>>();

        sdgs.stream().forEach(sdg -> {
            Map<String, Object> sdgMap = sdg.toMap((short) 0);
            sentiAnalysisTree.add(sdgMap);
        });
        map.put("text", input);
        map.put("sentiment", sentiment);
        map.put("sentimentWeight", sentiAnalysisTree.get(0).get("so"));
        map.put("analysisTree", sentiAnalysisTree);

        return map;
    }
}

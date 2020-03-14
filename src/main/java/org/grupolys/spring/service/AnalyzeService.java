package org.grupolys.spring.service;

import org.grupolys.profiles.exception.DictionaryNotFoundException;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.processor.Processor;
import org.grupolys.samulan.util.SentimentDependencyGraph;
import org.grupolys.samulan.util.SentimentInformation;
import org.grupolys.spring.model.persistence.PersistentAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyzeService {

    private final SamulanRulesService rulesService;
    private final SamulanProcessorService processorService;
    private final Processor processor;

    @Autowired
    public AnalyzeService(SamulanRulesService rulesService,
                          SamulanProcessorService processorService) {
        this.rulesService = rulesService;
        this.processorService = processorService;
        this.processor = processorService.getProcessor();
    }

    public PersistentAnalysis analyze(String profileId, String dictionaryId, String text)
            throws DictionaryNotFoundException {
        RuleBasedAnalyser rba = rulesService.getRules().get(dictionaryId);
        if (rba == null) {
            throw new DictionaryNotFoundException("Dictionary '" + dictionaryId + "' not found.");
        }

        List<SentimentDependencyGraph> sdgs = processor.process(text);
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

//        Map<String, Object> map = new HashMap<>();
        PersistentAnalysis analysis = new PersistentAnalysis();
        List<Map<String, Object>> sentiAnalysisTree = new ArrayList<>();

        sdgs.stream().forEach(sdg -> {
            Map<String, Object> sdgMap = sdg.toMap((short) 0);
            sentiAnalysisTree.add(sdgMap);
        });
        analysis.setText(text);
        analysis.setSentiment(sentiment);
        analysis.setAnalysisTree(sentiAnalysisTree);
        analysis.setDictionary(dictionaryId);
        analysis.setProfile(profileId);
        analysis.setSentimentWeight((float) sentiAnalysisTree.get(0).get("so"));

        return analysis;
    }
}

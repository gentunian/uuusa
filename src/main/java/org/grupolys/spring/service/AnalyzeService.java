package org.grupolys.spring.service;

import org.grupolys.profiles.exception.NoRulesLoadedForDictionaryException;
import org.grupolys.samulan.analyser.RuleBasedAnalyser;
import org.grupolys.samulan.processor.Processor;
import org.grupolys.samulan.util.PersistentSIGraph;
import org.grupolys.samulan.util.SentimentDependencyGraph;
import org.grupolys.samulan.util.SentimentInformation;
import org.grupolys.spring.model.persistence.PersistentAnalysis;
import org.grupolys.spring.model.responses.ErrorResponse;
import org.grupolys.spring.repositories.SentimentRepository;
import org.grupolys.spring.service.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyzeService {

    private final SamulanRulesService rulesService;
    private final SamulanProcessorService processorService;
    private final Processor processor;
    private final SentimentRepository sentimentRepository;

    @Autowired
    public AnalyzeService(SamulanRulesService rulesService,
                          SamulanProcessorService processorService,
                          SentimentRepository sentimentRepository) {
        this.rulesService = rulesService;
        this.processorService = processorService;
        this.processor = processorService.getProcessor();
        this.sentimentRepository = sentimentRepository;
    }

    public PersistentAnalysis analyze(String profileId, String dictionaryId, String text, boolean dryRun)
            throws NoRulesLoadedForDictionaryException {
        RuleBasedAnalyser rba = rulesService.getRules().get(dictionaryId);
        if (rba == null) {
            throw new NoRulesLoadedForDictionaryException("Dictionary '" + dictionaryId + "' has no rules loaded.");
        }

        List<SentimentDependencyGraph> sdgs = processor.process(text);
        List<SentimentInformation> sis = sdgs.stream()
                .map((SentimentDependencyGraph dg) -> (rba.analyse(dg, (short) 0))).collect(Collectors.toList());

        SentimentInformation si = rba.merge(sis);
        String sentiment;

        // Taken from Samulan.analyse method, please double check why not pos >= neg
        if (si.getPositiveSentiment() > si.getNegativeSentiment()
                || (si.getPositiveSentiment() == si.getNegativeSentiment() && si.getPositiveSentiment() > 0)) {
            sentiment = "1";
        } else if (si.getSemanticOrientation() < 0) {
            sentiment = "-1";
        } else {
            sentiment = "0";
        }

        PersistentAnalysis analysis = new PersistentAnalysis();
        List<PersistentSIGraph> sentiAnalysisTree = new ArrayList<>();

        sdgs.forEach(sdg -> {
            PersistentSIGraph graph = sdg.toPersistentSIGraph((short) 0);
            sentiAnalysisTree.add(graph);
        });
        analysis.setText(text);
        analysis.setSentiment(sentiment);
        analysis.setAnalysisTree(sentiAnalysisTree);
        analysis.setDictionary(dictionaryId);
        analysis.setProfile(profileId);
        analysis.setSentimentWeight(sentiAnalysisTree.get(0).getSo());

        if (!dryRun) {
            sentimentRepository.save(analysis);
        }
        return analysis;
    }

    /**
     * Gets an analysis from the db.
     * @param text the text to find.
     * @param dictionaryId the dictionary id that should belong to the analysis.
     * @return the analyasis
     * @throws ServiceException if no analysis is found.
     */
    public PersistentAnalysis getAnalysis(String text, String dictionaryId) throws ServiceException {
        PersistentAnalysis persistentAnalysis = sentimentRepository.findByTextAndDictionary(text, dictionaryId);
        if (persistentAnalysis == null) {
            throw new ServiceException(
                    new ErrorResponse("No analysis found with text '" + text + "'" +
                            " for dictionary with id '" + dictionaryId + "'", HttpStatus.NOT_FOUND)
            );
        }
        return persistentAnalysis;
    }
}

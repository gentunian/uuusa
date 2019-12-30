package org.grupolys.spring.service;

import java.io.File;

import org.grupolys.samulan.processor.Processor;
import org.grupolys.samulan.processor.parser.MaltParserWrapper;
import org.grupolys.samulan.processor.tagger.MaxentStanfordTagger;
import org.grupolys.samulan.processor.tokenizer.ARKTwokenizer;
import org.springframework.stereotype.Service;

@Service
public class SamulanProcessorService {

    private Processor processor = null;

    SamulanProcessorService() {
        ARKTwokenizer arktokenizer = new ARKTwokenizer();

        String taggerDir = "/opt/uuusa/data/taggers";

        File folderSentiData = new File(taggerDir);
        File[] files = folderSentiData.listFiles();
        String pathStanfordTagger = null;

        // This code looks for file ending with STANFORD_TAGGER_SUFFIX (.tagger)
        // and sets the absolute path in pathStanfordTagger
        for (File f : files) {
            if (f.getAbsolutePath().endsWith(".tagger")) {
                pathStanfordTagger = f.getAbsolutePath();
                break;
            }
        }
        // If no file was found throw an exception
        if (pathStanfordTagger == null) {
            System.out.println(".tagger file not found in SentiData");
        } else {
            // builds the tagger
            MaxentStanfordTagger tagger = new MaxentStanfordTagger(pathStanfordTagger);
            String parserDir = "/opt/uuusa/data/parsers/0";
            MaltParserWrapper parser = new MaltParserWrapper(parserDir);
            processor = new Processor(arktokenizer, tagger, parser);
        }
    }

    public Processor getProcessor() {
        return processor;
    }
}

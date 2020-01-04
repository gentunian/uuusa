package org.grupolys.samulan.util.dictionary;

import org.grupolys.samulan.analyser.operation.Operation;

import java.io.*;
import java.util.Arrays;

public class FilesystemDictionary extends AbstractDictionary {

    public enum LookupFiles {

        EMOTION_LIST("EmotionLookupTable.txt"),
        BOOSTER_LIST("BoosterWordList.txt"),
        LEMMAS_LIST("LemmasList.txt"),
        EMOTICON_LIST("EmoticonLookupTable.txt"),
        WORD_LIST("EnglishWordList.txt"),
        IDIOM_LIST("IdiomLookupTable.txt"),
        IRONY_LIST("IronyTerms.txt"),
        NEGATING_LIST("NegatingWordList.txt"),
        QUESTION_LIST("QuestionWords.txt"),
        SLANG_LIST("SlangLookupTable.txt"),
        ADVERSATIVE_LIST("AdversativeList.txt"),
        LEMMAS_STRIPPERS("WordToLemmasStrippingList.txt"),
        POSTAG_SEPARATOR("_");

        private String filename;

        LookupFiles(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return this.filename;
        }
    }

    private String basePath;

    /**
     * Builds a dictionary based on data stored in files.
     * @param path path to where files are located.
     */
    public FilesystemDictionary(String path) {
        super();
        basePath = path;
        readSentiData(basePath);
    }

    // TODO: this is the 1st refactoring. More refactoring needed, major and breaking changes
    private BufferedReader getBufferedReader(String path, String encoding) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return br;
    }

    // TODO: this is the 1st refactoring. More refactoring needed, major and breaking changes
    private void readSentiDataList(String pathToSentiDataFile, String classValue) {
        if (classValue != null) {
            setClassEmotionDict(true);
        }
        BufferedReader br;
        String line;
        br = getBufferedReader(pathToSentiDataFile, "utf-8");
        try {
            WordsValues values = new WordsValues();
            StemValues stemValues = new StemValues();
            ClassValues classValues = new ClassValues();
            while ((line = br.readLine()) != null) {
                String[] ls = line.split("\t");
                if (ls.length >= 2) {
                    String word = ls[0];
                    try {
                        float so = Float.parseFloat(ls[1]);

                        if (!word.endsWith("*")) {
                            values.setValue(null, word, so);
                        } else {
                            stemValues.setValue(null, word.replace("*", ""), so);
                        }

                        if (classValue != null) {
                            classValues.setValue(classValue, word, so);
                        }
                    } catch (NumberFormatException e) {
                        // ERROR reading file with two columns, but second column is not a float
                    }
                }
            }
            addWordsValues(values);
            addClassValues(classValues);
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: this is the 1st refactoring. More refactoring needed, major and breaking changes
    private void readBoosterList(String pathToSentiDataFile, String classValue) {
        BufferedReader br;
        String line;

        br = getBufferedReader(pathToSentiDataFile, "utf-8");
        try {
            ClassValues classValues = new ClassValues();
            while ((line = br.readLine()) != null) {
                String[] ls = line.split("\t");
                if (ls.length >= 2) {
                    String word = ls[0];
                    try {
                        float so = Float.parseFloat(ls[1]);
                        if (classValue != null) {
                            classValues.setValue(classValue, word, so);
                        }
                    } catch (NumberFormatException e) {
                        // ERROR reading file with two columns, but second column is not a float
                    }
                }
            }
            addClassValues(classValues);
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: this is the 1st refactoring. More refactoring needed, major and breaking changes
    private void readLemmaList(String pathToSentiDataFile, String classValue) {
        BufferedReader br;
        String line;

        br = getBufferedReader(pathToSentiDataFile, "utf-8");
        try {
            LemmasValues lemmas = new LemmasValues();
            ClassLemmasValues classLemmasValues = new ClassLemmasValues();
            while ((line = br.readLine()) != null) {
                String[] ls = line.split("\t");
                if (ls.length == 2) {
                    lemmas.setValue(null, ls[0], ls[1]);
                } else if (ls.length == 3) {
                    classLemmasValues.setValue(ls[0], ls[1], ls[2]);
                } else {
                    System.err.println("Non standard number of columns Lemmas_list file");
                }
            }
            addLemmasValues(lemmas);
            addClassLemmasValues(classLemmasValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: this is the 1st refactoring. More refactoring needed, major and breaking changes
    private void readEmoticonList(String pathToSentiDataFile) {
        BufferedReader br;
        String line;

        br = getBufferedReader(pathToSentiDataFile, "utf-8");
        try {
            EmoticonsValues emoticonsValues = new EmoticonsValues();
            while ((line = br.readLine()) != null) {
                String[] ls = line.split("\t");
                if (ls.length == 2) {
                    emoticonsValues.setValue(null, ls[0], Float.valueOf(ls[1]));
                } else {
                    System.err.println("Non standard number of columns on Emoticon_list file");
                }
            }
            setEmoticons(emoticonsValues.getValues());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: this is the 1st refactoring. More refactoring needed, major and breaking changes
    private void readLemmasStrippers(String pathToWordToLemmasStrippingList) {
        BufferedReader br;
        String line;

        br = getBufferedReader(pathToWordToLemmasStrippingList, "utf-8");
        try {
            LemmaStrippersValues lemmaStrippersValues = new LemmaStrippersValues();
            while ((line = br.readLine()) != null) {
                String[] ls = line.split("\t");
                if (ls.length == 2) {
                    lemmaStrippersValues.setValue(null, ls[0], Arrays.asList(ls[1].split(",")));
                } else {
                    System.err.println("Non standard number of columns on Emoticon_list file");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: this is the 1st refactoring. More refactoring needed, major and breaking changes
    private void readNegatingWordsList(String pathToSentiDataFile) {
        BufferedReader br;
        String line;

        br = getBufferedReader(pathToSentiDataFile, "utf-8");
        try {
            NegatingWordsValues negatingWordsValues = new NegatingWordsValues();
            while ((line = br.readLine()) != null) {
                String[] ls = line.split("\t");
                if (ls.length == 1) {
                    negatingWordsValues.setValue(null, ls[0], null);
                } else {
                    System.err.println("Non standard number of columns NegatingWordList.txt file");
                }
            }
            setNegatingWords(negatingWordsValues.getValues());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Files inside SentiData must be in utf-8 format.
     */
    private void readSentiData(String pathToSentiData) {
        File sentiData = new File(pathToSentiData);
        File[] sentiDataFiles = sentiData.listFiles();
        for (File f : sentiDataFiles) {
            if (f.isFile()) {
                String filename = f.getName();
                String filePath = f.getAbsolutePath();

                if (filename.equals(LookupFiles.EMOTION_LIST.getFilename())) {
                    readSentiDataList(filePath, null);
                } else if (filename.endsWith(LookupFiles.EMOTION_LIST.getFilename())) {
                    readSentiDataList(filePath, filename.split(LookupFiles.POSTAG_SEPARATOR.getFilename())[0]);
                }

                if (filename.equals(LookupFiles.BOOSTER_LIST.getFilename())) {
                    readBoosterList(filePath, Operation.WEIGHT);
                }

                if (filename.equals(LookupFiles.NEGATING_LIST.getFilename())) {
                    readNegatingWordsList(filePath);
                }
                if (filename.equals(LookupFiles.LEMMAS_LIST.getFilename())) {
                    readLemmaList(filePath, LookupFiles.LEMMAS_LIST.getFilename());
                }
                if (filename.equals(LookupFiles.EMOTICON_LIST.getFilename())) {
                    readEmoticonList(filePath);
                }
                if (filename.equals(LookupFiles.ADVERSATIVE_LIST.getFilename())) {
                    readNegatingWordsList(filePath);
                }
                if (filename.equals(LookupFiles.LEMMAS_STRIPPERS.getFilename())) {
                    readLemmasStrippers(filePath);
                }
            } else {
                // TODO show missing files
            }
        }
    }
}

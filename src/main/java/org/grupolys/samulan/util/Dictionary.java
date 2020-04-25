package org.grupolys.samulan.util;

import org.grupolys.samulan.analyser.operation.Operation;

import java.io.*;
import java.util.*;

@Deprecated
public class Dictionary implements org.grupolys.samulan.util.dictionary.Dictionary {
	
	public static final String EMOTION_LIST = "EmotionLookupTable.txt";
	public static final String BOOSTER_LIST =	"BoosterWordList.txt";
	public static final String LEMMAS_LIST = "LemmasList.txt";
	public static final String EMOTICON_LIST = "EmoticonLookupTable.txt";
//	public static final String WORD_LIST = "EnglishWordList.txt";
//	public static final String IDIOM_LIST = "IdiomLookupTable.txt";
//	public static final String IRONY_LIST = "IronyTerms.txt";
	public static final String NEGATING_LIST = "NegatingWordList.txt";
//	public static final String QUESTION_LIST = "QuestionWords.txt";
//	public static final String SLANG_LIST = "SlangLookupTable.txt";
	public static final String ADVERSATIVE_LIST ="AdversativeList.txt";
	public static final String LEMMAS_STRIPPERS = "WordToLemmasStrippingList.txt";
	public static final String POSTAG_SEPARATOR = "_";
			
/*
 * It Read and manages the content found in SentiData
 * 
 */

	private Map<String,Float> values;
	private TreeMap<String,Float> stemValues;
	private Map<String,Map<String,Float>> classValues;
	private Map<String,String> lemmas;
	private Map<String,Map<String,String>> classLemmas;
	private Map<String,Float> emoticons;
	private Set<String> negatingWords;
	private Set<String> adversativeWords;
	private Set<String> adverbsIntensifiers;
	private Map<String,ArrayList<String>> lemmasStrippers;
	private boolean thereIsClassEmotionDict;
	
	public Dictionary(){
		/*Empty dictionary*/
		this.values = new HashMap<>();
//		this.stemValues = new TreeMap<String,Float>();
		this.stemValues = new TreeMap<>((s1, s2) -> {
			if (s1.length() > s2.length()) {
				return -1;
			} else if (s1.length() < s2.length()) {
				return 1;
			} else {
				return s1.compareTo(s2);
			}
		});
		this.classValues = new HashMap<>();
		this.lemmas = new HashMap<>();
		this.classLemmas = new HashMap<>();
		this.emoticons = new HashMap<>();
		this.negatingWords = new HashSet<>();
		this.adversativeWords = new HashSet<>();
		this.adverbsIntensifiers = new HashSet<>();
		this.lemmasStrippers = new HashMap<>();
		this.thereIsClassEmotionDict = false;
	}
	
	
	public Dictionary(Map<String, Float> values, Map<String,String> lemmas){
		this.values = values;
		this.lemmas = lemmas;
		this.stemValues = new TreeMap<>((o1, o2) -> o2.length() - o1.length());
		this.classValues = new HashMap<>();
		this.classLemmas = new HashMap<>();
		this.emoticons = new HashMap<>();
		this.negatingWords = new HashSet<>();
		this.adversativeWords = new HashSet<>();
		this.adverbsIntensifiers = new HashSet<>();
		this.lemmasStrippers = new HashMap<>();
		this.thereIsClassEmotionDict = false;
	}
	
	public Dictionary(Map<String, Float> values,
					  Map<String, String> lemmas,
					  Map<String, Map<String, Float>> classValues,
					  Map<String, Map<String, String>> classLemmas,
					  Map<String, Float> emoticons,
					  Set<String> negatingWords,
					  Set<String> adversativeWords) {
		// TODO pass stemValues as parameter
		this.values = values;
		this.lemmas = lemmas;
		this.stemValues = new TreeMap<>((o1, o2) -> o2.length() - o1.length());
		this.classValues = classValues;
		this.classLemmas = classLemmas;
		this.emoticons = emoticons;
		this.negatingWords = negatingWords;
		this.adversativeWords = adversativeWords;
		this.adverbsIntensifiers = new HashSet<>();
		this.lemmasStrippers = new HashMap<>();
		this.thereIsClassEmotionDict = !getClassValues().isEmpty();
	}
	
	public Map<String, Float> getValues() {
		return values;
	}

	public void setValues(Map<String, Float> values) {
		this.values = values;
	}

	public Map<String, Map<String, Float>> getClassValues() {
		return classValues;
	}

	public void setClassValues(Map<String, Map<String, Float>> classValues) {
		this.classValues = classValues;
	}

	public Map<String, String> getLemmas() {
		return lemmas;
	}

	public void setLemmas(Map<String, String> lemmas) {
		this.lemmas = lemmas;
	}

	public Map<String, Map<String, String>> getClassLemmas() {
		return classLemmas;
	}

	public void setClassLemmas(Map<String, Map<String, String>> classLemmas) {
		this.classLemmas = classLemmas;
	}

	public Map<String, Float> getEmoticons() {
		return emoticons;
	}

	public void setEmoticons(Map<String, Float> emoticons) {
		this.emoticons = emoticons;
	}

	public Set<String> getAdversativeWords() {
		return adversativeWords;
	}

	public void setAdversativeWords(Set<String> adversativeWords) {
		this.adversativeWords = adversativeWords;
	}

	public Set<String> getAdverbsIntensifiers() {
		return adverbsIntensifiers;
	}

	public void setAdverbsIntensifiers(Set<String> adverbsIntensifiers) {
		this.adverbsIntensifiers = adverbsIntensifiers;
	}

	/**
	 * @return The semantic orientation of the lemma or zero if the word has no
	 *         subjectivity.
	 */
	private float getValue(String lemma) {
		Float value = values.get(lemma);
		if (value == null) {
			// TODO this is not efficient
			for (String stem : stemValues.keySet()) {
				if (lemma.startsWith(stem)) {
					return stemValues.get(stem);
				}
			}
			return 0;
		}
		return value;
	}

	// TODO: file operation has nothing to do with dictionary
	private BufferedReader getBufferedReader(String path, String encoding) {

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		return br;
		
	}

	// TODO: file operation has nothing to do with dictionary
	private void readSentiDataList(String pathToSentiDataFile, String classValue) {
		BufferedReader br;
		String line;
		br = getBufferedReader(pathToSentiDataFile, "utf-8");
		try {
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] ls = line.split("\t");
					if (ls.length >= 2) {
						String word = ls[0];
						try {
							float so = Float.parseFloat(ls[1]);
							if (!word.endsWith("*")) {
								values.put(word, so);
							} else {
								stemValues.put(word.replace("*", ""), so);
							}
							if (classValue != null) {
								if (classValues.containsKey(classValue)) {
									classValues.get(classValue).put(word, so);
								} else {
									Map<String, Float> aux = new HashMap<>();
									aux.put(word, so);
									classValues.put(classValue, aux);
								}
							}
						} catch (NumberFormatException e) {
							// ERROR reading file with two columns, but second column is not a float
						}
					}
				}
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	// TODO: file operation has nothing to do with dictionary
    private void readBoosterList(String pathToSentiDataFile, String classValue) {
        BufferedReader br;
        String line;

        br = getBufferedReader(pathToSentiDataFile, "utf-8");
        try {
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] ls = line.split("\t");
                    if (ls.length >= 2) {
                        String word = ls[0];
                        try {
                            float so = Float.parseFloat(ls[1]);
                            if (classValue != null) {
                                if (this.classValues.containsKey(classValue))
                                    this.classValues.get(classValue).put(word, so);
                                else {
                                    Map<String, Float> aux = new HashMap<>();
                                    aux.put(word, so);
                                    this.classValues.put(classValue, aux);
                                }
                            }
                        } catch (NumberFormatException e) {
                            // ERROR reading file with two columns, but second column is not a float
                        }
                    }
                }
            }
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }

	// TODO: file operation has nothing to do with dictionary
	private void readLemmaList(String pathToSentiDataFile, String classValue) {
		BufferedReader br;
		String line;
		
		br = getBufferedReader(pathToSentiDataFile, "utf-8");
		try {
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] ls = line.split("\t");
					if (ls.length == 2) {
						String word = ls[0];
						String lemma = ls[1];
						this.lemmas.put(word, lemma);
					} else {
						if (ls.length == 3) {
							
							String postag = ls[0];
							String word = ls[1];
							String lemma = ls[2];	
							Map<String, String> aux = new HashMap<>();
							if (this.classLemmas.containsKey(postag)) {
								this.classLemmas.get(postag).put(word, lemma);
							} else {
								aux.put(word, lemma);
								this.classLemmas.put(postag, aux);
							}
						} else {
							System.err.println("Non standard number of columns Lemmas_list file");
						}
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO: file operation has nothing to do with dictionary
	private void readEmoticonList(String pathToSentiDataFile) {
		
		BufferedReader br;
		String line;
		
		br = getBufferedReader(pathToSentiDataFile, "utf-8");
		try {
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] ls = line.split("\t");
					if (ls.length == 2) {
						String emoticon = ls[0];
						Float value = Float.valueOf(ls[1]);
						
						this.emoticons.put(emoticon, value);
						
					} else {
						System.err.println("Non standard number of columns on Emoticon_list file");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// TODO: file operation has nothing to do with dictionary
	private void readLemmasStrippers(String pathToWordToLemmasStrippingList) {
		
		BufferedReader br;
		String line;
		
		br = getBufferedReader(pathToWordToLemmasStrippingList, "utf-8");
		try {
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] ls = line.split("\t");
					if (ls.length == 2) {
						String emoticon = ls[0];
						ArrayList<String> strippers = new ArrayList<>(Arrays.asList(ls[1].split(",")));
						this.lemmasStrippers.put(emoticon, strippers);
					} else {
						System.err.println("Non standard number of columns on Emoticon_list file");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// TODO: file operation has nothing to do with dictionary
	private void readNegatingWordsList(String pathToSentiDataFile) {
		
		BufferedReader br;
		String line;
		br = getBufferedReader(pathToSentiDataFile, "utf-8");
		try {
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] ls = line.split("\t");
					if (ls.length == 1) {
						String negatingWord = ls[0];
						this.negatingWords.add(negatingWord);
					} else {
						System.err.println("Non standard number of columns NegatingWordList.txt file");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Files inside SentiData must be in utf-8 format.
	 * 	// TODO: file operation has nothing to do with dictionary
	 */
	public void readSentiData(String pathToSentiData) {
		
		File sentiData = new File(pathToSentiData);
		File[] sentiDataFiles = sentiData.listFiles();
		for (File f : sentiDataFiles) {
			if (f.isFile()) {
				
				if (f.getName().equals(EMOTION_LIST)) {
					readSentiDataList(f.getAbsolutePath(), null);
				} else if (f.getName().endsWith(EMOTION_LIST)) {
					this.thereIsClassEmotionDict = true;
					readSentiDataList(f.getAbsolutePath(), f.getName().split(POSTAG_SEPARATOR)[0]);
				}
				
				if (f.getName().equals(BOOSTER_LIST)) {
					readBoosterList(f.getAbsolutePath(), Operation.WEIGHT);
				}
				
				if (f.getName().equals(NEGATING_LIST)) {
					readNegatingWordsList(f.getAbsolutePath());
				}
				if (f.getName().equals(LEMMAS_LIST)) {
					readLemmaList(f.getAbsolutePath(), LEMMAS_LIST);
				}
				if (f.getName().equals(EMOTICON_LIST)) {
					readEmoticonList(f.getAbsolutePath());
				}
				if (f.getName().equals(ADVERSATIVE_LIST)) {
					readNegatingWordsList(f.getAbsolutePath());
				}
				if (f.getName().equals(LEMMAS_STRIPPERS)) {
					readLemmasStrippers(f.getAbsolutePath());
				}
			} else {
				// TODO show missing files
			}
		}
		// this.getAdverbsIntensfiers();
	}
	
	public boolean isWeight(String lemma) {
		if (classValues.get(Operation.WEIGHT) == null) {
			return false;
		}
		return classValues.get(Operation.WEIGHT).get(lemma) != null;
	}

	@Override
	public Set<String> getBoosterWords() {
		return this.classValues.get(Operation.WEIGHT).keySet();
	}

	@Override
	public float getBoosterValue(String word) {
		if (this.classValues.get(Operation.WEIGHT).get(word) != null) {
			return this.classValues.get(Operation.WEIGHT).get(word);
		}
		return 0;
	}

	/**
	 * @return The semantic orientation of the lemma or zero if the word has no
	 *         subjectivity.
	 */
	public float getValue(String classWord, String lemma, boolean relaxed) {

		Float value = null;
		String lowerCaseLemma = lemma.toLowerCase();
		Map<String, Float> values = classValues.get(classWord);
		if (values != null) {
			value = values.get(lowerCaseLemma);
			if (adverbsIntensifiers.contains(lowerCaseLemma)) {
				// System.out.println("Entra getValue: "+lowerCaseLemma);
				// value = (float) 0;
			}
		}
		
		if (((values == null || value == null) && relaxed)
				|| ((values == null || value == null) && !thereIsClassEmotionDict)) {
			value = getValue(lowerCaseLemma);
		}
		
		if (emoticons.containsKey(lowerCaseLemma)) {
			value = emoticons.get(lowerCaseLemma);
		}
		
		if (value == null)
			return 0;
		
		System.out.println("Dictionary.getValue: classWord: "+ classWord +" lemma: "+lemma + " value: "+value);
		return value;
	}
	
	public Set<String> getNegatingWords() {
		return negatingWords;
	}

	public void setNegatingWords(Set<String> negatingWords) {
		this.negatingWords = negatingWords;
	}

	/**
	 * @return The lemma of the word or the word itself if no lemma was found
	 */
	private String getLemma(String word) {
		String lemma = lemmas.get(word);
		if (lemma == null) {
			return word;
		}
		return lemma;
	}
	
	/**
	 * @return The lemma of the word or the word itself if no lemma was found
	 */	
	
	public String getLemma(String postag, String word) {
		// TODO this functions is the one converting words to lowercase, is there a
		// better option?
		String wordLowerCase = word.toLowerCase();
		String lemma;
		Map<String, String> lemmas = classLemmas.get(postag);

		if (lemmas == null) {
			return getLemma(wordLowerCase);
		}
		lemma = lemmas.get(wordLowerCase); 
		if (lemma == null) {
			return wordLowerCase;
		}
		return lemma;
	}
	
	/**
	 * Gets a possible lemma by stripping the end of the word, based on substrings
	 * that represent suffixes
	 * 
	 * @param postag Part-of-speech tag of the word
	 * @param word a word
	 * @return a string
	 */
	public String getStrippedLemma(String postag, String word) {
		String wordLowerCase = word.toLowerCase();
		String lemma = null;
		ArrayList<String> postagLemmasStrippers = this.lemmasStrippers.get(postag);
		if (postagLemmasStrippers != null) {
			for (String pls : postagLemmasStrippers) {
				if (word.endsWith(pls)) {
					lemma = word.substring(0, word.length() - pls.length());
				}
			}
		}
		// System.out.println("word: "+wordLowerCase+" lemmaStripped: "+lemma);
		if (lemma == null) {
			return wordLowerCase;
		}
		return lemma;
		
	}

	@Override
	public void setClassEmotionDict(boolean classEmotionDict) {
		thereIsClassEmotionDict = classEmotionDict;
	}

	@Override
	public boolean getClassEmotionDict() {
		return thereIsClassEmotionDict;
	}

	public void addHashMapValues(Map<String, Float> values) {
		this.values.putAll(values);
	}
	
	public void addHashMapLemmas(Map<String, String> lemmas) {
		this.lemmas.putAll(lemmas);
	}
	
	public void addPostagHashMapValues(String operation, Map<String, Float> values) {
		if (!operation.equals(Operation.WEIGHT)) {
			this.thereIsClassEmotionDict = true;
		}
		classValues.put(operation, values);
	}
	
	public void addPostagHashMapLemmas(String postag, Map<String, String> lemmas) {
		classLemmas.put(postag, lemmas);
	}
	
}

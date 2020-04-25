package org.grupolys.samulan.rule;

import org.apache.commons.lang.NotImplementedException;
import org.grupolys.samulan.analyser.operation.*;
import org.grupolys.samulan.util.SentimentDependencyGraph;
import org.grupolys.samulan.util.SentimentDependencyNode;
import org.grupolys.samulan.util.SentimentInformation;
import org.grupolys.samulan.util.dictionary.Dictionary;
import org.grupolys.samulan.util.exceptions.OperationNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * It manages the rules of the system, reading the configuration.xml and identifies
 * the rules that match with a node of a @see es.udc.fi.dc.lys.nlputils.parser.DependencyGraph
 *
 * @author David Vilares
 */
public class XmlRulesManager implements RulesManager {

    //XML fields
    public static final String OPERATION_FIELD = "operation";
    public static final String FORM = "form";
    public static final String POSTAG = "postag";
    public static final String DEPENDENCY = "dependency";
    public static final String LEVELSUP = "levelsup";
    public static final String TYPE = "type";
    public static final String PRIORITY = "priority";
    public static final String VALID_HEAD = "validhead";

    //Special forms
    public static final String SENTIDATA = "SENTIDATA";
    public static final String SENTIDATA_BOOSTER = "SENTIDATA_BOOSTER";
    public static final String SENTIDATA_NEGATION = "SENTIDATA_NEGATION";
    public static final String SENTIDATA_ADVERSATIVE = "SENTIDATA_ADVERSATIVE";

    //Scopes
    public static final String HEAD = "DEST";
    public static final String CHILDREN = "CHILDREN";
    public static final String N_RIGHT_CHILDREN = "RCN";
    public static final String N_LEFT_CHILDREN = "LCN";
    private static final String FIRST_SUBJECTIVE_RIGHT_BRANCH = "SUBJR";
    private static final String FIRST_SUBJECTIVE_LEFT_BRANCH = "SUBJL";


    private List<Rule> rules;
    private Dictionary dictionary;
    private boolean alwaysShift = true;
    private Pattern alphaNumericPattern = Pattern.compile("^[\\-'a-zA-Z0-9]");
    private Pattern nonAlphaNumericPattern = Pattern.compile("[^.+]");

    public XmlRulesManager(Dictionary dictionary, String rulesPath) {
        this.rules = new ArrayList<>();
        this.dictionary = dictionary;
        this.rules.add(new Rule());
        this.readRules(rulesPath);
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public void addRule(Rule r) {
        this.rules.add(0, r);
    }


    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        throw new NotImplementedException("setDictionary() not implemented.");
    }


    public boolean isAlwaysShift() {
        return alwaysShift;
    }

    public void setAlwaysShift(boolean alwaysShift) {
        this.alwaysShift = alwaysShift;
    }

    public List<Operation> getOperations(SentimentDependencyGraph dg, short address) {
        SentimentDependencyNode currentNode = dg.getNode(address);
        List<Operation> operations = new ArrayList<>();

        for (Rule r : rules) {
            if (r.match(dg, currentNode)) {
                Operation o = r.getOperation();

                if (currentNode.getSi() == null) {
                    // TODO: try to avoid side-effects in a method named getOperation()
					currentNode.setSi(new SentimentInformation());
				}

                if (!o.getOperationName().equals(Operation.DEFAULT)) {
                    // TODO: try to avoid side-effects in a method named getOperation()
                    currentNode.getSi().setType(o.getOperationName());

                    if (o.getStrategy() instanceof NChildrenStrategy) {
                        // TODO: try to avoid side-effects in a method named getOperation()
                        ((NChildrenStrategy) o.getStrategy()).setReference(address);
                    }

                    if (o.getStrategy() instanceof FirstSubjectiveChildrenStrategy) {
                        // TODO: try to avoid side-effects in a method named getOperation()
                        ((FirstSubjectiveChildrenStrategy) o.getStrategy()).setReference(address);
                    }
                }

                operations.add(r.getOperation());
            }
        }

        if (operations.contains(getOperation(Operation.DEFAULT)) && operations.size() > 1) {
			operations.remove(getOperation(Operation.DEFAULT));
		}

        return operations;
    }

    private String getXMLNodeValue(Element element, String XMLTag) {
        NodeList fstNmElmntLst = element.getElementsByTagName(XMLTag);
        Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
        NodeList fstNm = fstNmElmnt.getChildNodes();
        String value = fstNm.item(0).getNodeValue();
        return value;
    }

    private Operation getOperation(String operation) {
        //null because rule can't be established in this point
        if (operation.equals(Operation.DEFAULT)) {
        	return new DefaultOperation(null);
		}
        //TODO: This could be removed now. Double check first
//		if (operation.startsWith(Operation.SHIFT)) return new ShiftOperation(null,null,-4);
//		if (operation.startsWith(Operation.WEIGHT)) return new WeightingOperation(null,null,1);
        try {
            throw new OperationNotFoundException();
        } catch (OperationNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     * Creates a new operation based on the field <type> of a <rule> at configuration.xml
     *
     * @param operation
     * @param form      It will correspond to a word form in BOOSTER_LIST or NEGATION_LIST was
     *                  specified at the field <form> a a <rule>. Null if the word form specified at configuration.xml
     * @return ... or NULL if operation was not parsed.
     */
    private Operation getOperationFinal(String operation, String form) {
        Operation o = null;

        if (operation.startsWith(Operation.SHIFT)) {
            o = this.parseShiftOperation(operation, form);
        }

        if (operation.startsWith(Operation.WEIGHT)) {
            o = this.parseWeightOperation(operation, form);
        }

        return o;
    }

    private ScopeStrategy getScopeStrategy(String scope, String[] parameters) {
        if (scope.startsWith(N_RIGHT_CHILDREN)) {
            return new NChildrenStrategy(Integer.parseInt(scope.replace(N_RIGHT_CHILDREN, "")), true);
        }

        if (scope.startsWith(N_LEFT_CHILDREN)) {
            return new NChildrenStrategy(Integer.parseInt(scope.replace(N_LEFT_CHILDREN, "")), false);
        }

        if (scope.equals(HEAD)) {
            return new HeadStrategy();
        }

        if (scope.equals(CHILDREN)) {
            return new HeadStrategy(true);
        }

        if (scope.equals(FIRST_SUBJECTIVE_LEFT_BRANCH)) {
            return new FirstSubjectiveChildrenStrategy(false);
        }

        if (scope.equals(FIRST_SUBJECTIVE_RIGHT_BRANCH)) {
            return new FirstSubjectiveChildrenStrategy(true);
        }

        return new BranchStrategy(scope);
    }

    private Operation parseShiftOperation(String operation, String form) {
        ShiftOperation successor = null;
        ScopeStrategy strategy;
        Pattern p = Pattern.compile("(\\(.*\\))");
        Matcher m = p.matcher(operation);
        m.find();
        String[] parameters = m.group(1)
				.replace("(", "")
				.replace(")", "")
				.split(",");

        float shiftValue;
        if (parameters[0].equals(SENTIDATA)) {
            // current code does not uses Operation.SHIFT in dictionary
//            shiftValue = dictionary.getValue(Operation.SHIFT, form, true);
            shiftValue = 0;
        } else {
        	shiftValue = Float.parseFloat(parameters[0]);
		}

        for (int i = parameters.length - 1; i >= 0; i--) {
            strategy = getScopeStrategy(parameters[i], parameters);
            successor = new ShiftOperation(null, strategy, successor, shiftValue);
            successor.setAlwaysShift(isAlwaysShift());
        }

        return successor;
    }


    private Operation parseWeightOperation(String operation, String form) {
        WeightingOperation successor = null;
        ScopeStrategy strategy;
        Pattern p = Pattern.compile("(\\(.*\\))");
        Matcher m = p.matcher(operation);
        m.find();
        String[] parameters = m.group(1)
                .replace("(", "")
                .replace(")", "")
                .split(",");

        float weightingValue;
        if (parameters[0].equals(SENTIDATA)) {
            // getValue(Operation.WEIGHT) refers to booster words.
//            weightingValue = dictionary.getValue(Operation.WEIGHT, form, true);
            weightingValue = (float) dictionary.getBoosterValue(form);
        } else {
            weightingValue = Float.parseFloat(parameters[0]);
        }

        for (int i = parameters.length - 1; i >= 1; i--) {
            strategy = getScopeStrategy(parameters[i], parameters);
            successor = new WeightingOperation(null, strategy, weightingValue, successor);
        }

        return successor;

    }

    private boolean isNonRegexForm(String form) {
        return alphaNumericPattern.matcher(form).matches();
    }

    private boolean isRegexForm(String form) {
        return nonAlphaNumericPattern.matcher(form).matches();
    }


    private void addRules(Element fstElement,
                          Set<String> sentiForms,
                          Set<String> postags,
                          Set<String> dependencies,
                          short levelsup,
                          short priority,
                          Set<String> validHead) {

        for (String sentiForm : sentiForms) {
            Operation o = getOperationFinal(getXMLNodeValue(fstElement, TYPE), sentiForm);

            if (o != null) {
                Set<String> patternForms = new HashSet<>();
                Set<Pattern> patterns = new HashSet<>();

                if (isNonRegexForm(sentiForm)) {
                    patterns = new HashSet<>(Collections.singletonList(Pattern.compile(sentiForm)));
                } else {
                    patternForms = new HashSet<>(Collections.singletonList(sentiForm));
                }
                Rule r = new Rule(patterns, patternForms, postags, dependencies, levelsup, priority, validHead, o);
                addRule(r);
            }
        }
    }

    public void readRules(String pathXML) {
        try {
            File file = new File(pathXML);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            //			  readAnalyserConfiguration(doc.getElementsByTagName("conf"));

            NodeList nodeLst = doc.getElementsByTagName(OPERATION_FIELD);
            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element fstElement = (Element) fstNode;
                    Set<String> forms = new HashSet<>(Arrays.asList(this.getXMLNodeValue(fstElement, FORM).split(",")));
                    Set<String> postags = new HashSet<>(Arrays.asList(this.getXMLNodeValue(fstElement, POSTAG).split(",")));
                    Set<String> dependencies = new HashSet<>(Arrays.asList(this.getXMLNodeValue(fstElement, DEPENDENCY).split(",")));
                    short levelsup = new Short(this.getXMLNodeValue(fstElement, LEVELSUP));
                    short priority = new Short(this.getXMLNodeValue(fstElement, PRIORITY));
                    HashSet<String> validHead = new HashSet<>(Arrays.asList(this.getXMLNodeValue(fstElement, VALID_HEAD).split(",")));
                    postags.remove("*"); //TODO process regex
                    dependencies.remove("*"); //TODO process regex
                    validHead.remove("*"); //TODO process regex

                    //INTENSIFICATION RULES WITH SENTIDATA
                    if (forms.contains(SENTIDATA_BOOSTER)) {
                        if (forms.size() == 1) {
                            //System.out.println("getClassValues: "+this.d.getClassValues());
                            Set<String> boosterWords = dictionary.getBoosterWords();
                            if (boosterWords != null) {
                                addRules(fstElement, boosterWords, postags, dependencies, levelsup, priority,
                                        validHead);
                            }
                        } else {
                            System.err.println("We cannot handle ");
                        }
                    }

                    //NEGATION RULES WITH SENTIDATA
                    else if (forms.contains(SENTIDATA_NEGATION)) {
                        if (forms.size() == 1) {
                            Set<String> negatingWords = dictionary.getNegatingWords();
                            if (negatingWords != null) {
                                addRules(fstElement, negatingWords,
                                        postags, dependencies, levelsup, priority,
                                        validHead);
                            }
                        } else {
                            System.err.println("We cannot handle this kind of rules " + forms);
                        }

                    }
                    //OTHER RULES
                    else {
                        forms.remove("*"); //TODO process regex
                        if (forms.contains(SENTIDATA_ADVERSATIVE)) {
                            forms.addAll(this.dictionary.getAdversativeWords());
                        }
                        //System.out.println(forms+" "+priority);
                        addRules(fstElement, forms,
                                postags, dependencies, levelsup, priority,
                                validHead);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package org.grupolys.profiles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Map<String, Float>> emotions;

    private Map<String, Float> emoticons;

    private Map<String, Float> boosters;

    private List<String> negating;

    public void mergeProfile(Profile profile) {
        if (profile.getEmotions() != null) {
            if (this.emotions == null) {
                this.emotions = new HashMap<String, Map<String, Float>>();
            }
            for (String pos : profile.getEmotions().keySet()) {
                Map<String, Float> m = profile.getEmotions().get(pos);
                String posAlias = PartOfSpeech.getPartOfSpeech(pos);
                if (this.emotions.containsKey(posAlias)) {
                    this.emotions.get(posAlias).putAll(m);
                } else {
                    this.emotions.put(posAlias, m);
                }
            }
        }

        if (profile.getEmoticons() != null) {
            if (this.emoticons == null) {
                this.emoticons = new HashMap<String, Float>();
            }
            this.emoticons.putAll(profile.getEmoticons());
        }

        if (profile.getBoosters() != null) {
            if (this.boosters == null) {
                this.boosters = new HashMap<String, Float>();
            }
            this.boosters.putAll(profile.getBoosters());
        }

        if (profile.getNegating() != null) {
            if (this.negating == null) {
                this.negating = new ArrayList<String>();
            }
            this.negating.addAll(profile.getNegating());
        }
    }

    /**
     * @return the emotions
     */
    public Map<String, Map<String, Float>> getEmotions() {
        return emotions;
    }

    /**
     * @return the negating
     */
    public List<String> getNegating() {
        return negating;
    }

    /**
     * @return the boosters
     */
    public Map<String, Float> getBoosters() {
        return boosters;
    }

    /**
     * @return the emoticons
     */
    public Map<String, Float> getEmoticons() {
        return emoticons;
    }

    /**
     * @param emotions the emotions to set
     */
    public void setEmotions(Map<String, Map<String, Float>> emotions) {
        this.emotions = emotions;
    }

    /**
     * @param emoticons the emoticons to set
     */
    public void setEmoticons(Map<String, Float> emoticons) {
        this.emoticons = emoticons;
    }

    /**
     * @param negating the negating to set
     */
    public void setNegating(List<String> negating) {
        this.negating = negating;
    }

    /**
     * @param boosters the boosters to set
     */
    public void setBoosters(Map<String, Float> boosters) {
        this.boosters = boosters;
    }

    public void addEmotion(String pos, String word, Float weight) {
        pos = PartOfSpeech.getPartOfSpeech(pos);
        if (this.emotions == null) {
            this.emotions = new HashMap<String, Map<String, Float>>();
        }
        if (this.emotions.containsKey(pos)) {
            this.emotions.get(pos).put(word, weight);
        } else {
            Map<String, Float> m = new HashMap<String, Float>();
            m.put(word, weight);
            this.emotions.put(pos, m);
        }
    }

    public void removeEmotion(String pos, String word) {
        if (this.emotions != null) {
            this.emotions.get(pos).remove(word);
        }
    }

    public void addNegatingWord(String word) {
        if (this.negating == null) {
            this.negating = new ArrayList<String>();
        }
        this.negating.add(word);
    }

    public void removeNegatingWord(String word) {
        if (this.negating != null) {
            this.negating.remove(word);
        }
    }

    public void addBoosterWord(String word, Float value) {
        if (this.boosters == null) {
            this.boosters = new HashMap<String, Float>();
        }
        this.boosters.put(word, value);
    }

    public void removeBoosterWord(String word) {
        if (this.boosters != null) {
            this.boosters.remove(word);
        }
    }
}

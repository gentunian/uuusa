package org.grupolys.profiles;

import org.grupolys.profiles.exception.ProfileNotFoundException;

import java.util.List;

public interface DictionaryProfileStore {
    /**
     * Stores profile information with name {@literal}profileName.
     *
     * @param profileName the name of the profile. This is useful to name the
     *                    profile.
     * @param profile     the profile to store.
     */
    boolean saveProfile(String profileName, DictionaryProfile profile);

    /**
     * Loads profile information for a profile named {@literal}profileName.
     *
     * @param profileName the name of the profile to load
     * @return the loaded Profile (null if profile does not exist)
     */
    DictionaryProfile loadProfile(String profileName) throws ProfileNotFoundException;

    /**
     * @return list of saved profiles
     */
    List<DictionaryProfile> profiles();
}

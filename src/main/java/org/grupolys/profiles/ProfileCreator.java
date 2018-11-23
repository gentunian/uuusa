package org.grupolys.profiles;

import org.grupolys.profiles.exception.ProfileNotFoundException;

public interface ProfileCreator {
    /**
     * Stores profile information with name {@literal}profileName.
     * 
     * @param profileName the name of the profile. This is useful to name the
     *                    profile.
     * @param Profile     the profile to store.
     */
    public boolean saveProfile(String profileName, Profile profile);

    /**
     * Loads profile information for a profile named {@literal}profileName.
     * 
     * @param profileName the name of the profile to load
     * @return the loaded Profile (null if profile does not exist)
     */
    public Profile loadProfile(String profileName) throws ProfileNotFoundException;
}
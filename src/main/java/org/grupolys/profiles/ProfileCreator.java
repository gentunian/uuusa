package org.grupolys.profiles;

import org.grupolys.profiles.exception.ProfileNotFoundException;

interface ProfileCreator {
    /**
     * Stores profile information with name {@literal}profileName.
     * 
     * @param profileName the name of the profile. This is useful to name the
     *                    profile.
     * @param profile     the profile to store.
     */
    boolean saveProfile(String profileName, Profile profile);

    /**
     * Loads profile information for a profile named {@literal}profileName.
     * 
     * @param profileName the name of the profile to load
     * @return the loaded Profile (null if profile does not exist)
     */
    Profile loadProfile(String profileName) throws ProfileNotFoundException;

    /**
     * @return list of saved profiles
     */
    String[] profiles();
}

// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.oauth2.useragent;

import java.util.List;
import java.util.Map;

public interface ProviderScanner {

    /**
     * Scans the {@link Provider} implementations, checks their requirements
     * and tries to find one that will work, preferring the one named by the
     * {@code userAgentProvider} property, if it was set.
     *
     * @return a {@link Provider} that was deemed compatible;
     *         {@code null} if no compatible providers were found.
     */
    Provider findCompatibleProvider();

    /**
     * Scans the {@link Provider} implementations, checks their requirements
     * and tries to find one that will work, preferring the one whose
     * name was provided.
     *
     * @param  userAgentProvider the name of the preferred provider or
     *                           {@code null} if there's no preference
     * @return a {@link Provider} that was deemed compatible;
     *         {@code null} if no compatible providers were found.
     */
    Provider findCompatibleProvider(final String userAgentProvider);

    /**
     * Determines unmet requirements (if any) for each available {@link Provider} implementation.
     *
     * @return a map of {@link Provider} instances to their list of unmet requirements.
     *         If a {@link Provider} had all its requirements met, it will not be part of the map.
     *         An empty map means all providers are compatible.
     */
    Map<Provider, List<String>> getUnmetProviderRequirements();

    /**
     * Calls {@link #findCompatibleProvider()} and
     * returns whether a compatible {@link Provider} was found.
     *
     * @return {@code true} if a compatible provider was found;
     *         {@code false} otherwise.
     */
    boolean hasCompatibleProvider();

    /**
     * Calls {@link #findCompatibleProvider(String)} and
     * returns whether a compatible {@link Provider} was found.
     *
     * @param  userAgentProvider the name of the preferred provider or
     *                           {@code null} if there's no preference
     * @return {@code true} if a compatible provider was found;
     *         {@code false} otherwise.
     */
    boolean hasCompatibleProvider(final String userAgentProvider);

}

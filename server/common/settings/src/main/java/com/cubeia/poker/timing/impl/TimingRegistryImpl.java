/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.poker.timing.impl;

import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.poker.timing.TimingRegistry;
import com.cubeia.poker.timing.Timings;

/**
 * Simple implementation of the TimingRegistry
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class TimingRegistryImpl implements TimingRegistry {

    private TimingProfile defaultProfile = new DefaultTimingProfile();

    public TimingProfile getDefaultTimingProfile() {
        return defaultProfile;
    }

    public TimingProfile getTimingProfile(String profile) {
        if (profile.equals("SLOW")) {
            return new SlowTimingProfile();
        } else if (profile.equals("DEFAULT")) {
            return getDefaultTimingProfile();
        } else if (profile.equals("MINIMUM_DELAY")) {
            return new MinDelayTimingProfile();
        } else if (profile.equals("EXPRESS")) {
            return new ExpressTimingProfile();
        } else if (profile.equals("SUPER_EXPRESS")) {
            return new SuperExpressTimingProfile();
        } else {
            return findCustomProfile(profile);
        }
    }

    private TimingProfile findCustomProfile(String profile) {

        return getDefaultTimingProfile();
    }

}

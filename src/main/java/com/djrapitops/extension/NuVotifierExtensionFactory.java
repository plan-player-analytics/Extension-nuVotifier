/*
    Copyright(c) 2019 Risto Lahtela (Rsl1122)

    The MIT License(MIT)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files(the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions :
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/
package com.djrapitops.extension;

import com.djrapitops.plan.extension.DataExtension;

import java.util.Optional;

/**
 * Factory for DataExtension.
 *
 * @author Rsl1122
 */
public class NuVotifierExtensionFactory {

    private boolean isAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public Optional<DataExtension> createExtension() {
        if (!isAvailable("com.vexsoftware.votifier.model.Vote")) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(createNewExtension());
        } catch (IllegalStateException noSponge) {
            return Optional.empty();
        }
    }

    private DataExtension createNewExtension() {
        if (isAvailable("org.bukkit.event.EventHandler")) {
            return new BukkitNuVotifierExtension();
        }
        if (isAvailable("net.md_5.bungee.event.EventHandler")) {
            return new BungeeNuVotifierExtension();
        }
        if (isAvailable("org.spongepowered.api.event.Listener")) {
            return new SpongeNuVotifierExtension();
        }
        return null;
    }
}
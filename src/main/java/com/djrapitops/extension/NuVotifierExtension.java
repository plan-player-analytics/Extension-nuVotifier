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
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;

import java.util.Map;

public abstract class NuVotifierExtension implements DataExtension {

    private final NuVotifierStorage storage;

    NuVotifierExtension(NuVotifierStorage storage) {
        this.storage = storage;
    }

    @NumberProvider(
            text = "Votes",
            description = "Times the player has voted",
            iconName = "vote-yea",
            iconColor = Color.TEAL,
            showInPlayerTable = true
    )
    public long votes(String playerName) {
        return storage.getVoteCount(playerName);
    }

    @TableProvider(tableColor = Color.TEAL)
    public Table serviceVotesTable() {
        Map<String, Integer> votesPerService = storage.getVotesPerService();

        Table.Factory table = Table.builder()
                .columnOne("Service", Icon.called("poll-h").build())
                .columnTwo("Votes", Icon.called("vote-yea").build());

        votesPerService.entrySet().stream()
                .sorted((one, two) -> Integer.compare(two.getValue(), one.getValue()))
                .forEach(entry -> table.addRow(entry.getKey(), entry.getValue()));

        return table.build();
    }
}

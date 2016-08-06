/*
 * Copyright (c) 2016 Lucko (Luck) <luck@lucko.me>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.commands.group;

import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.commands.Sender;
import me.lucko.luckperms.commands.SingleMainCommand;
import me.lucko.luckperms.constants.Message;
import me.lucko.luckperms.constants.Permission;
import me.lucko.luckperms.utils.Patterns;

import java.util.List;

public class CreateGroup extends SingleMainCommand {
    public CreateGroup() {
        super("CreateGroup", "/%s creategroup <group>", 1, Permission.CREATE_GROUP);
    }

    @Override
    protected void execute(LuckPermsPlugin plugin, Sender sender, List<String> args, String label) {
        if (args.size() == 0) {
            sendUsage(sender, label);
            return;
        }

        String groupName = args.get(0).toLowerCase();

        if (groupName.length() > 36) {
            Message.GROUP_NAME_TOO_LONG.send(sender, groupName);
            return;
        }

        if (Patterns.NON_ALPHA_NUMERIC.matcher(groupName).find()) {
            Message.GROUP_INVALID_ENTRY.send(sender);
            return;
        }

        plugin.getDatastore().loadGroup(groupName, success -> {
            if (success) {
                Message.GROUP_ALREADY_EXISTS.send(sender);
            } else {
                plugin.getDatastore().createAndLoadGroup(groupName, success1 -> {
                    if (!success1) {
                        Message.CREATE_GROUP_ERROR.send(sender);
                    } else {
                        Message.CREATE_SUCCESS.send(sender, groupName);
                        plugin.runUpdateTask();
                    }
                });
            }
        });
    }
}

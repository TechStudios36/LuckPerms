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

package me.lucko.luckperms.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.luckperms.LuckPermsPlugin;
import me.lucko.luckperms.constants.Message;
import me.lucko.luckperms.constants.Permission;
import me.lucko.luckperms.groups.Group;
import me.lucko.luckperms.tracks.Track;
import me.lucko.luckperms.users.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Abstract SubCommand class
 */
@Getter
@AllArgsConstructor
public abstract class SubCommand<T> {

    /**
     * The name of the sub command
     */
    private final String name;

    /**
     * A brief description of what the sub command does
     */
    private final String description;

    /**
     * The command usage
     */
    private final String usage;

    /**
     * The permission needed to use this command
     */
    private final Permission permission;

    /**
     * Predicate to test if the argument length given is invalid
     */
    private final Predicate<? super Integer> isArgumentInvalid;

    /**
     * Called when this sub command is ran
     * @param plugin a link to the main plugin instance
     * @param sender the sender to executed the command
     * @param t the object the command is operating on
     * @param args the stripped arguments given
     * @param label the command label used
     */
    public abstract void execute(LuckPermsPlugin plugin, Sender sender, T t, List<String> args, String label);

    /**
     * Send the command usage to a sender
     * @param sender the sender to send the usage to
     * @param label the command label used
     */
    public void sendUsage(Sender sender, String label) {
        Util.sendPluginMessage(sender, "&e-> &d" + String.format(getUsage(), label));
    }

    /**
     * If a sender has permission to use this command
     * @param sender the sender trying to use the command
     * @return true if the sender can use the command
     */
    public boolean isAuthorized(Sender sender) {
        return permission.isAuthorized(sender);
    }

    /**
     * Returns a list of suggestions, which are empty by default. Sub classes that give tab complete suggestions override
     * this method to give their own list.
     * @param sender who is tab completing
     * @param args the arguments so far
     * @param plugin the plugin instance
     * @return a list of suggestions
     */
    public List<String> onTabComplete(Sender sender, List<String> args, LuckPermsPlugin plugin) {
        return Collections.emptyList();
    }

    /*
        ----------------------------------------------------------------------------------
        Utility methods used by #onTabComplete and #execute implementations in sub classes
        ----------------------------------------------------------------------------------
     */

    protected static List<String> getGroupTabComplete(List<String> args, LuckPermsPlugin plugin) {
        return getTabComplete(new ArrayList<>(plugin.getGroupManager().getGroups().keySet()), args);
    }

    protected static List<String> getTrackTabComplete(List<String> args, LuckPermsPlugin plugin) {
        return getTabComplete(new ArrayList<>(plugin.getTrackManager().getTracks().keySet()), args);
    }

    protected static List<String> getBoolTabComplete(List<String> args) {
        if (args.size() == 2) {
            return Arrays.asList("true", "false");
        } else {
            return Collections.emptyList();
        }
    }

    private static List<String> getTabComplete(List<String> options, List<String> args) {
        if (args.size() <= 1) {
            if (args.isEmpty() || args.get(0).equalsIgnoreCase("")) {
                return options;
            }

            return options.stream().filter(s -> s.toLowerCase().startsWith(args.get(0).toLowerCase())).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    protected static void saveUser(User user, Sender sender, LuckPermsPlugin plugin) {
        user.refreshPermissions();

        plugin.getDatastore().saveUser(user, success -> {
            if (success) {
                Message.USER_SAVE_SUCCESS.send(sender);
            } else {
                Message.USER_SAVE_ERROR.send(sender);
            }
        });
    }

    protected static void saveGroup(Group group, Sender sender, LuckPermsPlugin plugin) {
        plugin.getDatastore().saveGroup(group, success -> {
            if (success) {
                Message.GROUP_SAVE_SUCCESS.send(sender);
            } else {
                Message.GROUP_SAVE_ERROR.send(sender);
            }

            plugin.runUpdateTask();
        });
    }

    protected static void saveTrack(Track track, Sender sender, LuckPermsPlugin plugin) {
        plugin.getDatastore().saveTrack(track, success -> {
            if (success) {
                Message.TRACK_SAVE_SUCCESS.send(sender);
            } else {
                Message.TRACK_SAVE_ERROR.send(sender);
            }

            plugin.runUpdateTask();
        });
    }
}

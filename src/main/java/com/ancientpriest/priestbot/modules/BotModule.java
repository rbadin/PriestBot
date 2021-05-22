package com.ancientpriest.priestbot.modules;

import com.ancientpriest.priestbot.Channel;

public interface BotModule {

    public void onMessage(Channel channel, String sender, String login, String hostname, String message);

    public void onSelfMessage(Channel channel, String sender, String message);

    public void onJoin(Channel channel, String sender, String login, String hostname);

    public void onPart(Channel channel, String sender, String login, String hostname);

}

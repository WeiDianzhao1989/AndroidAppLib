package com.weidian.plugin;

import com.weidian.plugin.core.ctx.Plugin;

public interface PluginMsgMatcher {
    /**
     * 只有未指定toId时,即toId为空, 才调用这个方法.
     *
     * @param plugin
     * @return 是否要将消息发送给这个插件
     */
    boolean match(Plugin plugin);
}

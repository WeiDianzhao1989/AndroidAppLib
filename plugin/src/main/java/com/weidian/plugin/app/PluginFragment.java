package com.weidian.plugin.app;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

import com.weidian.plugin.core.ctx.Host;
import com.weidian.plugin.core.ctx.Plugin;

/**
 * Created by krystaljake on 16/5/30.
 */
public class PluginFragment extends Fragment {

    private final Plugin plugin;

    public PluginFragment() {
        super();
        this.plugin = Plugin.getPlugin(this);
    }

    public final Context getPluginContext() {
        return this.plugin.getContext();
    }

    public final Resources getPluginResources() {
        return plugin.getContext().getResources();
    }

    public final LayoutInflater getPluginLayoutInflater(Bundle savedInstanceState) {
        if (Host.class.isInstance(plugin)) {
            return super.getLayoutInflater(savedInstanceState);
        } else {
            return plugin.getLayoutInflater();
        }
    }
}

/*
 * Copyright (C) 2015 The Dirty Unicorns Project
 * Copyright (C) 2017 Benzo Rom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.qs.QSHost;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NavBarTile extends QSTileImpl<BooleanState> {
    private boolean mListening;
    private NavBarObserver mObserver;

    private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_navbar_on);

    public NavBarTile(QSHost host) {
        super(host);
        mObserver = new NavBarObserver(mHandler);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        toggleState();
        refreshState();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.MAGICAL_WORLD;
    }

    @Override
    public void handleLongClick() {
    }

    protected void toggleState() {
        Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.NAVIGATION_BAR_ENABLED, !NavBarEnabled() ? 1 : 0);
        Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.HARDWARE_KEYS_DISABLE, !NavBarEnabled() ? 0 : 1);
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_navbar_title);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (state.slash == null) {
            state.slash = new SlashState();
        }
        state.icon = mIcon;
        state.value = NavBarEnabled();
        if (state.value) {
            state.slash.isSlashed = false;
            state.state = Tile.STATE_ACTIVE;
            state.label = mContext.getString(R.string.quick_settings_navbar);
        } else {
            state.slash.isSlashed = true;
            state.state = Tile.STATE_INACTIVE;
            state.label = mContext.getString(R.string.quick_settings_navbar_off);
        }
    }

    private boolean NavBarEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_ENABLED, 0) == 1;
    }

    @Override
    public void handleSetListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            mObserver.startObserving();
        } else {
            mObserver.endObserving();
        }
    }

    private class NavBarObserver extends ContentObserver {
        public NavBarObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            refreshState();
        }

        public void startObserving() {
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.NAVIGATION_BAR_ENABLED),
                    false, this);
        }

        public void endObserving() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }
    }
}


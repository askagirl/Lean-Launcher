/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.nexuslauncher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.widget.WidgetsBottomSheet;
import com.hdeva.launcher.LeanSettings;
import com.hdeva.launcher.LeanUtils;

public class CustomBottomSheet extends WidgetsBottomSheet {
    private FragmentManager mFragmentManager;
    private EditText editText;

    public CustomBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFragmentManager = Launcher.getLauncher(context).getFragmentManager();
    }

    @Override
    public void populateAndShow(final ItemInfo itemInfo) {
        super.populateAndShow(itemInfo);
        editText = (EditText) findViewById(R.id.title);
        editText.setText(itemInfo.title);
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    handleAppNameChange(v.getContext(), editText.getText().toString(), itemInfo.getTargetComponent());
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                handleAppNameChange(v.getContext(), editText.getText().toString(), itemInfo.getTargetComponent());
                return true;
            }
        });
        ((PrefsFragment) mFragmentManager.findFragmentById(R.id.sheet_prefs)).loadForApp(itemInfo);
    }

    @Override
    public void onDetachedFromWindow() {
        Fragment pf = mFragmentManager.findFragmentById(R.id.sheet_prefs);
        if (pf != null) {
            mFragmentManager.beginTransaction().remove(pf).commitAllowingStateLoss();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWidgetsBound() {
    }

    private void handleAppNameChange(Context context, String newName, ComponentName componentName) {
        if (editText != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
        LeanSettings.setCustomAppName(context, componentName, newName);
        LeanUtils.reload(context);
    }

    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        private final static String PREF_HIDE = "pref_app_hide";
        private SwitchPreference mPrefHide;

        private String mComponentName;
        private String mPackageName;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_edit_prefs);
        }

        public void loadForApp(ItemInfo itemInfo) {
            mComponentName = itemInfo.getTargetComponent().toString();
            mPackageName = itemInfo.getTargetComponent().getPackageName();

            mPrefHide = (SwitchPreference) findPreference(PREF_HIDE);

            Context context = getActivity();
            CustomDrawableFactory factory = (CustomDrawableFactory) DrawableFactory.get(context);

//            ComponentName componentName = itemInfo.getTargetComponent();
//            boolean enable = factory.packCalendars.containsKey(componentName) || factory.packComponents.containsKey(componentName);
//            mPrefPack.setEnabled(enable);
//            mPrefPack.setChecked(enable && CustomIconProvider.isEnabledForApp(context, mComponentName));
//            if (enable) {
//                PackageManager pm = context.getPackageManager();
//                try {
//                    mPrefPack.setSummary(pm.getPackageInfo(factory.iconPack, 0).applicationInfo.loadLabel(pm));
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }

            mPrefHide.setChecked(CustomAppFilter.isHiddenApp(context, mComponentName, mPackageName));

//            mPrefPack.setOnPreferenceChangeListener(this);
            mPrefHide.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean enabled = (boolean) newValue;
            Launcher launcher = Launcher.getLauncher(getActivity());
            switch (preference.getKey()) {
//                case PREF_PACK:
//                    CustomIconProvider.setAppState(launcher, mComponentName, enabled);
//                    CustomIconUtils.reloadIcons(launcher, mPackageName);
//                    break;
                case PREF_HIDE:
                    CustomAppFilter.setComponentNameState(launcher, mComponentName, mPackageName, enabled);
                    break;
            }
            return true;
        }
    }
}

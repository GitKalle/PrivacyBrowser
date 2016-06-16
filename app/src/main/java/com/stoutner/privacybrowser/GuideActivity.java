/**
 * Copyright 2016 Soren Stoutner <soren@stoutner.com>.
 *
 * This file is part of Privacy Browser <https://www.stoutner.com/privacy-browser>.
 *
 * Privacy Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Browser.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stoutner.privacybrowser;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class GuideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_coordinatorlayout);

        // We need to use the SupportActionBar from android.support.v7.app.ActionBar until the minimum API is >= 21.
        Toolbar supportAppBar = (Toolbar) findViewById(R.id.guide_toolbar);
        setSupportActionBar(supportAppBar);

        // Display the home arrow on supportAppBar.
        final ActionBar appBar = getSupportActionBar();
        assert appBar != null;// This assert removes the incorrect warning in Android Studio on the following line that appBar might be null.
        appBar.setDisplayHomeAsUpEnabled(true);

        //  Setup the ViewPager.
        ViewPager aboutViewPager = (ViewPager) findViewById(R.id.guide_viewpager);
        assert aboutViewPager != null; // This assert removes the incorrect warning in Android Studio on the following line that aboutViewPager might be null.
        aboutViewPager.setAdapter(new guidePagerAdapter(getSupportFragmentManager()));

        // Setup the TabLayout and connect it to the ViewPager.
        TabLayout aboutTabLayout = (TabLayout) findViewById(R.id.guide_tablayout);
        assert aboutTabLayout != null; // This assert removes the incorrect warning in Android Studio on the following line that aboutTabLayout might be null.
        aboutTabLayout.setupWithViewPager(aboutViewPager);
    }

    public class guidePagerAdapter extends FragmentPagerAdapter {
        public guidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        // Get the count of the number of tabs.
        public int getCount() {
            return 8;
        }

        @Override
        // Get the name of each tab.  Tab numbers start at 0.
        public CharSequence getPageTitle(int tab) {
            switch (tab) {
                case 0:
                    return getString(R.string.overview);

                case 1:
                    return getString(R.string.javascript);

                case 2:
                    return getString(R.string.local_storage);

                case 3:
                    return getString(R.string.user_agent);

                case 4:
                    return getString(R.string.tor);

                case 5:
                    return getString(R.string.tracking_uids);

                case 6:
                    return getString(R.string.clear_and_exit);

                case 7:
                    return getString(R.string.planned_features);

                default:
                    return "";
            }
        }

        @Override
        // Setup each tab.
        public Fragment getItem(int tab) {
            return GuideTabFragment.createTab(tab);
        }
    }

}

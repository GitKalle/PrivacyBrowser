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
import android.support.v7.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_linearlayout);

        //  Setup the ViewPager.
        ViewPager aboutViewPager = (ViewPager) findViewById(R.id.about_viewpager);
        assert aboutViewPager != null; // This assert removes the incorrect warning on the following line that aboutViewPager might be null.
        aboutViewPager.setAdapter(new aboutPagerAdapter(getSupportFragmentManager()));

        // Setup the TabLayout and connect it to the ViewPager.
        TabLayout aboutTabLayout = (TabLayout) findViewById(R.id.about_tablayout);
        assert aboutTabLayout != null; // This assert removes the incorrect warning on the following line that aboutTabLayout might be null.
        aboutTabLayout.setupWithViewPager(aboutViewPager);
    }

    public class aboutPagerAdapter extends FragmentPagerAdapter {
        public aboutPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        // Get the count of the number of tabs.
        public int getCount() {
            return 6;
        }

        @Override
        // Get the name of each tab.  Tab numbers start at 0.
        public CharSequence getPageTitle(int tab) {
            switch (tab) {
                case 0:
                    return getString(R.string.version);

                case 1:
                    return getString(R.string.permissions);

                case 2:
                    return getString(R.string.privacy_policy);

                case 3:
                    return getString(R.string.changelog);

                case 4:
                    return getString(R.string.license);

                case 5:
                    return getString(R.string.contributors);

                default:
                    return "";
            }
        }

        @Override
        // Setup each tab.
        public Fragment getItem(int tab) {
            return AboutTabFragment.createTab(tab);
        }
    }


}

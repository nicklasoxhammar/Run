package oxhammar.nicklas.run.activities;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import oxhammar.nicklas.run.fragments.CurrentRunFragment;
import oxhammar.nicklas.run.fragments.FinishedRunsFragment;
import oxhammar.nicklas.run.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;


public class MainActivity extends AppCompatActivity implements CurrentRunFragment.OnFragmentInteractionListener, FinishedRunsFragment.OnFragmentInteractionListener {

    FinishedRunsFragment finishedRunsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SectionsPagerAdapter sectionsPagerAdapter;
        ViewPager viewPager;

        // primary sections of the activity.
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);

            if (position == 0) {
                return CurrentRunFragment.newInstance();
            } else {
                finishedRunsFragment = FinishedRunsFragment.newInstance();
                return finishedRunsFragment;
            }

        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void addFinishedRun() {

        finishedRunsFragment.addFinishedRunToRecyclerView();
    }

    //Don't destroy activity on back button pressed
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

}



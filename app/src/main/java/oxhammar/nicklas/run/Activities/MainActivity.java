package oxhammar.nicklas.run.Activities;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import oxhammar.nicklas.run.Fragments.CurrentRunFragment;
import oxhammar.nicklas.run.Fragments.FinishedRunsFragment;
import oxhammar.nicklas.run.R;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.view.View;


public class MainActivity extends AppCompatActivity implements CurrentRunFragment.OnFragmentInteractionListener, FinishedRunsFragment.OnFragmentInteractionListener {


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    FinishedRunsFragment finishedRunsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);

            if (position == 0){
                return CurrentRunFragment.newInstance();
            }else{
                finishedRunsFragment = finishedRunsFragment.newInstance();
                return finishedRunsFragment;
            }

        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    public void addFinishedRun(){

        finishedRunsFragment.addFinishedRunToRecyclerView();
    }

}



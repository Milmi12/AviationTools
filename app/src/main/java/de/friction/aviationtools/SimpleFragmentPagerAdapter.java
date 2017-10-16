package de.friction.aviationtools;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Provides the appropriate {@link Fragment} for a view pager.
 */
public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    private Context mContext;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new FuelCalcFragment();
        } else if (position == 1){
            return new FDTCheckFragment();
        } else {
            return new WeekLimitCheckFragment();
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.label_fuelcalc);
        } else if (position == 1){
            return mContext.getString(R.string.label_fdtcheck);
        } else {
            return mContext.getString(R.string.label_weeklycheck);
        }
    }
}
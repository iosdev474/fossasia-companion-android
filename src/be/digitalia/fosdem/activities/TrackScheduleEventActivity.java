package be.digitalia.fosdem.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import be.digitalia.fosdem.R;
import be.digitalia.fosdem.db.DatabaseManager;
import be.digitalia.fosdem.fragments.EventDetailsFragment;
import be.digitalia.fosdem.loaders.TrackScheduleLoader;
import be.digitalia.fosdem.model.Day;
import be.digitalia.fosdem.model.Track;

import com.viewpagerindicator.PageIndicator;

/**
 * Event view of the track schedule; allows to slide between events of the same track using a ViewPager.
 * 
 * @author Christophe Beyls
 * 
 */
public class TrackScheduleEventActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

	public static final String EXTRA_DAY = "day";
	public static final String EXTRA_TRACK = "track";
	public static final String EXTRA_POSITION = "position";

	private static final int EVENTS_LOADER_ID = 1;

	private Day day;
	private Track track;
	private int initialPosition = -1;
	private View progress;
	private ViewPager pager;
	private TrackScheduleEventAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.track_schedule_event);

		progress = findViewById(R.id.progress);
		pager = (ViewPager) findViewById(R.id.pager);
		adapter = new TrackScheduleEventAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		PageIndicator pageIndicator = (PageIndicator) findViewById(R.id.indicator);
		pageIndicator.setViewPager(pager);

		Bundle extras = getIntent().getExtras();
		day = extras.getParcelable(EXTRA_DAY);
		track = extras.getParcelable(EXTRA_TRACK);
		if (savedInstanceState == null) {
			initialPosition = extras.getInt(EXTRA_POSITION, -1);
		}

		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle(R.string.event_details);
		bar.setSubtitle(track.getName());

		setCustomProgressVisibility(true);
		getSupportLoaderManager().initLoader(EVENTS_LOADER_ID, null, this);
	}

	private void setCustomProgressVisibility(boolean isVisible) {
		progress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new TrackScheduleLoader(this, day, track);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		setCustomProgressVisibility(false);

		if (data != null) {
			adapter.setCursor(data);
			if (initialPosition != -1) {
				pager.setCurrentItem(initialPosition, false);
				initialPosition = -1;
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.setCursor(null);
	}

	public static class TrackScheduleEventAdapter extends FragmentStatePagerAdapter {

		private Cursor cursor;

		public TrackScheduleEventAdapter(FragmentManager fm) {
			super(fm);
		}

		public Cursor getCursor() {
			return cursor;
		}

		public void setCursor(Cursor cursor) {
			this.cursor = cursor;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return (cursor == null) ? 0 : cursor.getCount();
		}

		@Override
		public Fragment getItem(int position) {
			cursor.moveToPosition(position);
			return EventDetailsFragment.newInstance(DatabaseManager.toEvent(cursor));
		}
	}
}

package me.iwf.photopicker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.entity.CompressedPhotoEntity;
import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.fragment.ImagePagerFragment;
import me.iwf.photopicker.fragment.PhotoPickerFragment;

import static android.widget.Toast.LENGTH_LONG;

public class PhotoPickerActivity extends AppCompatActivity {

  private PhotoPickerFragment pickerFragment;
  private ImagePagerFragment imagePagerFragment;

  public final static String EXTRA_MAX_COUNT     = "MAX_COUNT";
  public final static String EXTRA_SHOW_CAMERA   = "SHOW_CAMERA";
  public final static String KEY_SELECTED_PHOTOS = "SELECTED_PHOTOS";
  public final static String KEY_FILTER_PHOTOS = "SELECTED_FILTER";

  private MenuItem menuDoneItem;

  public final static int DEFAULT_MAX_COUNT = 9;

  private int maxCount = DEFAULT_MAX_COUNT;

  private ArrayList<CompressedPhotoEntity> filterPhotos;

  /** to prevent multiple calls to inflate menu */
  private boolean menuIsInflated = false;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_photo_picker);

    Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
    mToolbar.setTitle(R.string.images);

    setSupportActionBar(mToolbar);

    ActionBar actionBar = getSupportActionBar();

    actionBar.setDisplayHomeAsUpEnabled(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      actionBar.setElevation(25);
    }

    filterPhotos = (ArrayList<CompressedPhotoEntity>) getIntent().getSerializableExtra(KEY_FILTER_PHOTOS);
    maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
    boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);

    pickerFragment =
        (PhotoPickerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPickerFragment);

    pickerFragment.getPhotoGridAdapter().setShowCamera(showCamera);

    pickerFragment.setFilterPhotos(filterPhotos);

    pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
      @Override public boolean OnItemCheck(int position, Photo photo, final boolean isCheck, int selectedItemCount) {

        int total = selectedItemCount + (isCheck ? -1 : 1);

        menuDoneItem.setEnabled(total > 0);

        if (maxCount <= 1) {
          List<Photo> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
          photos.clear();
          pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
          return true;
        }

        if (total > maxCount) {
          Toast.makeText(getActivity(), getString(R.string.over_max_count_tips, maxCount),
              LENGTH_LONG).show();
          return false;
        }
        menuDoneItem.setTitle(getString(R.string.done_count, total));
        return true;
      }
    });

  }


  /**
   * Overriding this method allows us to run our exit animation first, then exiting
   * the activity when it complete.
   */
  @Override public void onBackPressed() {
    if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
      imagePagerFragment.runExitAnimation(new Runnable() {
        public void run() {
          if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();//立即执行事务
          }
        }
      });
    } else {
      super.onBackPressed();
    }
  }


  public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
    this.imagePagerFragment = imagePagerFragment;
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.container, this.imagePagerFragment)
        .addToBackStack(null)
        .commitAllowingStateLoss();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    if (!menuIsInflated) {
      getMenuInflater().inflate(R.menu.menu_picker, menu);
      menuDoneItem = menu.findItem(R.id.done);
      menuDoneItem.setEnabled(false);
      menuIsInflated = true;
      return true;
    }
    return false;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
      return true;
    }

    if (item.getItemId() == R.id.done) {
      Intent intent = new Intent();
      ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
      intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
      setResult(RESULT_OK, intent);
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public PhotoPickerActivity getActivity() {
    return this;
  }
}

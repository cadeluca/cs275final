package cs275.gaspricetracker;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ImageViewFragment extends DialogFragment {
    private static final String ARG_PHOTO = "photo";
    private static final String ARG_HAS_IMAGE = "hasImage";
    private static boolean mHasImage;
    private ImageView mPhotoView;

    public static ImageViewFragment newInstance(String photoPath, String string) {
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO, photoPath);
        args.putString(ARG_HAS_IMAGE, string);

        ImageViewFragment fragment = new ImageViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String photoPath = getArguments().getString(ARG_PHOTO);
        String hasImage = getArguments().getString(ARG_HAS_IMAGE);
        mHasImage = hasImage != "local";
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_image, null);
        mPhotoView = v.findViewById(R.id.photo_zoom);
        if (!mHasImage) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(photoPath, getActivity());
            mPhotoView.setImageBitmap(bitmap);
        } else {
            // images hosted on Junda's account for the purpose of this class
            String url = "https://jtan5.w3.uvm.edu/cs275/" + photoPath;
            Picasso.get().load(url).into(mPhotoView);
        }

        return new AlertDialog.Builder(getActivity())
                .setView(v).create();
    }
}
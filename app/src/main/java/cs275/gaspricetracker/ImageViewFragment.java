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

    private ImageView mPhotoView;

    public static ImageViewFragment newInstance(String photoFilename) {
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO, photoFilename);

        ImageViewFragment fragment = new ImageViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String photoFilename = getArguments().getString(ARG_PHOTO);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_image, null);

        mPhotoView = (ImageView) v.findViewById(R.id.photo_zoom);
        Bitmap bitmap = PictureUtils.getScaledBitmap(photoFilename, getActivity());

        String url = "https://jtan5.w3.uvm.edu/cs275/" + photoFilename;
        Picasso.get().load(url).into(mPhotoView);
        //mPhotoView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(v).create();
    }
}
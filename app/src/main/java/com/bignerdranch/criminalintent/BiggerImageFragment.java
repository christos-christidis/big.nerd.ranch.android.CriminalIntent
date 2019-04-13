package com.bignerdranch.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import java.io.File;
import java.util.Objects;

public class BiggerImageFragment extends DialogFragment {

    private static final String ARG_FILE = "arg_file";

    static BiggerImageFragment newInstance(File file) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILE, file);

        BiggerImageFragment fragment = new BiggerImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(getArguments());
        File file = (File) getArguments().get(ARG_FILE);
        Objects.requireNonNull(file);
        Objects.requireNonNull(getActivity());

        // I already set the image equal to screen with the conservative estimate
        Bitmap bitmap = PictureUtils.getConservativeEstimateBitmap(file.getPath(), getActivity());
        ImageView dialogView = new ImageView(getActivity());
        dialogView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setView(dialogView)
                .create();
    }
}

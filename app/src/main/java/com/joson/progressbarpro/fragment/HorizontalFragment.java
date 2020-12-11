package com.joson.progressbarpro.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.joson.progress.view.horizontal.HorizontalProgressView;
import com.joson.progressbarpro.R;

/**
 * @Auther: hepiao
 * @CreatTime: 2020/9/25
 * @Description:
 */
public class HorizontalFragment extends Fragment {

    private HorizontalProgressView horizontalProgressView;
    private HorizontalProgressView horizontalProgressView2;
    private HorizontalProgressView horizontalProgressView3;
    private HorizontalProgressView horizontalProgressView4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_horizontal, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        horizontalProgressView = requireView().findViewById(R.id.horizontalProgress);
        horizontalProgressView2 = requireView().findViewById(R.id.horizontalProgress2);
        horizontalProgressView3 = requireView().findViewById(R.id.horizontalProgress3);
        horizontalProgressView4 = requireView().findViewById(R.id.horizontalProgress4);
        Button startButton = requireActivity().findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horizontalProgressView.setProgress(100);
                horizontalProgressView2.setProgress(80);
                horizontalProgressView3.setProgress(60);
                horizontalProgressView4.setProgress(40);
            }
        });
        Button resetButton = requireActivity().findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horizontalProgressView.setProgress(0);
                horizontalProgressView2.setProgress(0);
                horizontalProgressView3.setProgress(0);
                horizontalProgressView4.setProgress(0);
            }
        });
    }
}
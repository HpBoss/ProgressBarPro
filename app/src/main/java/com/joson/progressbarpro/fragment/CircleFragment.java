package com.joson.progressbarpro.fragment;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.joson.progress.view.circle.ChargeProgressView;
import com.joson.progress.view.circle.CircleProgressView;
import com.joson.progressbarpro.R;

/**
 * @Auther: hepiao
 * @CreatTime: 2020/9/23
 * @Description:
 */
public class CircleFragment extends Fragment {
    private CircleProgressView circleProgressView;
    private CircleProgressView circleProgressView2;
    private CircleProgressView circleProgressView3;
    private CircleProgressView circleProgressView4;
    private ChargeProgressView chargeProgressView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_circle, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button startButton = requireActivity().findViewById(R.id.circle_start);
        Button resetButton = requireActivity().findViewById(R.id.circle_reset);
        circleProgressView = requireView().findViewById(R.id.circleProgress);
        circleProgressView2 = requireView().findViewById(R.id.circleProgress2);
        circleProgressView3 = requireView().findViewById(R.id.circleProgress3);
        circleProgressView4 = requireView().findViewById(R.id.circleProgress4);
        chargeProgressView = requireView().findViewById(R.id.chargeProgressView);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleProgressView.setProgress(90);
                circleProgressView2.setProgress(80);
                circleProgressView3.setProgress(100);
                circleProgressView4.setProgress(90);
                chargeProgressView.setProgress(100);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleProgressView.setProgress(0);
                circleProgressView2.setProgress(0);
                circleProgressView3.setProgress(0);
                circleProgressView4.setProgress(0);
                chargeProgressView.setProgress(0);
            }
        });

    }
}
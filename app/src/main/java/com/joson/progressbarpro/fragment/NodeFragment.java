package com.joson.progressbarpro.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.joson.progress.view.node.NodeProgressView;
import com.joson.progressbarpro.R;

/**
 * @Auther: hepiao
 * @CreatTime: 2020/9/23
 * @Description:
 */
public class NodeFragment extends Fragment {
    Button lastButton;
    Button nextButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_node, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final NodeProgressView nodeProgressView = requireView().findViewById(R.id.nodeProgressView);
        lastButton = requireView().findViewById(R.id.lastButton);
        nextButton = requireView().findViewById(R.id.nextButton);
        lastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodeProgressView.backStage();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodeProgressView.addStage();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
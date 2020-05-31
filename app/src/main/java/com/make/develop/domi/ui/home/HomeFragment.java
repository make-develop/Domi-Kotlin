package com.make.develop.domi.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.loopingviewpager.LoopingViewPager;
import com.make.develop.domi.Adapter.MyBestDealsAdapter;
import com.make.develop.domi.Adapter.MyPopularCategoriesAdapter;
import com.make.develop.domi.Model.BestDealModel;
import com.make.develop.domi.Model.PopularCategoryModel;
import com.make.develop.domi.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    Unbinder unbinder;

    @BindView(R.id.recycler_popular)
    RecyclerView recycler_popular;
    @BindView(R.id.viewpager)
    LoopingViewPager viewpager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        unbinder = ButterKnife.bind(this, root);
        init();
        homeViewModel.getPopularList().observe(this, new Observer<List<PopularCategoryModel>>() {
            @Override
            public void onChanged(List<PopularCategoryModel> popularCategoryModels) {

                //Create Adapter
                MyPopularCategoriesAdapter adapter = new MyPopularCategoriesAdapter(getContext(), popularCategoryModels);
                recycler_popular.setAdapter(adapter);
            }
        });

        homeViewModel.getBestDealList().observe(this, new Observer<List<BestDealModel>>() {
            @Override
            public void onChanged(List<BestDealModel> bestDealModels) {
                MyBestDealsAdapter adapter = new MyBestDealsAdapter(getContext(), bestDealModels, true);
                viewpager.setAdapter(adapter);
            }
        });
        return root;
    }

    private void init() {
        recycler_popular.setHasFixedSize(true);
        recycler_popular.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewpager.resumeAutoScroll();
    }

    @Override
    public void onPause() {
        viewpager.pauseAutoScroll();
        super.onPause();
    }
}

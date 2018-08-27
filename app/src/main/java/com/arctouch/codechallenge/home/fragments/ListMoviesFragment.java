package com.arctouch.codechallenge.home.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.arctouch.codechallenge.R;
import com.arctouch.codechallenge.base.BaseFragment;
import com.arctouch.codechallenge.model.Movie;
import com.arctouch.codechallenge.util.PaginationScrollListener;
import com.arctouch.codechallenge.util.ViewModelFactory;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by brunodelhferreira on 26/08/2018.
 */
public class ListMoviesFragment extends BaseFragment implements MovieSelectedListener {

    @BindView(R.id.recyclerView)
    RecyclerView listView;
    @BindView(R.id.tv_error)
    TextView errorTextView;
    @BindView(R.id.loading_view)
    View loadingView;

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES = 10;
    private int currentPage = PAGE_START;

    LinearLayoutManager linearLayoutManager;

    @Inject
    ViewModelFactory viewModelFactory;
    private ListMoviesViewModel viewModel;

    PaginationAdapter adapter;

    @Override
    protected int layoutRes() {
        return R.layout.screen_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ListMoviesViewModel.class);
        listView.addItemDecoration(new DividerItemDecoration(getBaseActivity(), DividerItemDecoration.VERTICAL));
        Log.d("---->", "Entrou 01");
        //listView.setAdapter(new MovieListAdapter(viewModel, this, this));

        adapter = new PaginationAdapter(viewModel, this, this);

        listView.setAdapter(adapter);
        Log.d("---->", "Entrou 02");

        //listView.setLayoutManager(new LinearLayoutManager(getContext()));
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(linearLayoutManager);
        Log.d("---->", "Entrou 03");
        listView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
                    @Override
                    protected void loadMoreItems() {
                        isLoading = true;
                        currentPage += 1;
                        Log.d("---->", "Entrou 04");

                        // mocking network delay for API call
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadNextPage();
                            }
                        }, 1000);
                    }

                    @Override
                    public int getTotalPageCount() {
                        return TOTAL_PAGES;
                    }

                    @Override
                    public boolean isLastPage() {
                        return isLastPage;
                    }

                    @Override
                    public boolean isLoading() {
                        return isLoading;
                    }
                });


        observableViewModel();
    }

    private void loadNextPage(){
        Long myLong= new Long(currentPage);
        viewModel.getReposNext(myLong).observe(this, repos -> {
            if(repos != null) listView.setVisibility(View.VISIBLE);
            adapter.addAll(repos);
        });

    }
    /*
    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callTopRatedMoviesApi().enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                List<Movie> results = fetchResults(response);
                adapter.addAll(results);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                t.printStackTrace();
                // TODO: 08/11/16 handle failure
            }
        });
    } */

    @Override
    public void onMovieSelected(Movie movie) {
        /*
        DetailsViewModel detailsViewModel = ViewModelProviders.of(getBaseActivity(), viewModelFactory).get(DetailsViewModel.class);
        detailsViewModel.setSelectedRepo(repo);
        getBaseActivity().getSupportFragmentManager().beginTransaction().replace(R.id.screenContainer, new DetailsFragment())
                .addToBackStack(null).commit(); */
    }

    private void observableViewModel() {
        viewModel.getRepos().observe(this, repos -> {
            if(repos != null) listView.setVisibility(View.VISIBLE);
        });

        viewModel.getError().observe(this, isError -> {
            if (isError != null) if(isError) {
                errorTextView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                errorTextView.setText("An Error Occurred While Loading Data!");
            }else {
                errorTextView.setVisibility(View.GONE);
                errorTextView.setText(null);
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    errorTextView.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                }
            }
        });
    }
}
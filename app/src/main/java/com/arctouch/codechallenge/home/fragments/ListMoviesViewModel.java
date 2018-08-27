package com.arctouch.codechallenge.home.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.arctouch.codechallenge.api.ServiceMovies;
import com.arctouch.codechallenge.model.Movie;
import com.arctouch.codechallenge.model.UpcomingMoviesResponse;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class ListMoviesViewModel extends ViewModel {

    //private final RepoRepository repoRepository;
    private final ServiceMovies serviceMovies;
    private CompositeDisposable disposable;

    private final MutableLiveData<List<Movie>> movies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> repoLoadError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();

    @Inject
    public ListMoviesViewModel(ServiceMovies serviceMovies) {
        this.serviceMovies = serviceMovies;
        disposable = new CompositeDisposable();
        fetchRepos();
    }

    LiveData<List<Movie>> getRepos() {
        return movies;
    }
    LiveData<List<Movie>> getReposNext(Long page) {
        fetchReposNext(page);
        return movies;
    }
    LiveData<Boolean> getError() {
        return repoLoadError;
    }
    LiveData<Boolean> getLoading() {
        return loading;
    }

    /*
            api.upcomingMovies(TmdbApi.API_KEY, TmdbApi.DEFAULT_LANGUAGE, 1L, TmdbApi.DEFAULT_REGION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    for (Movie movie : response.results) {
                        movie.genres = new ArrayList<>();
                        for (Genre genre : Cache.getGenres()) {
                            if (movie.genreIds.contains(genre.id)) {
                                movie.genres.add(genre);
                            }
                        }
                    }

                    recyclerView.setAdapter(new HomeAdapter(response.results));
                    progressBar.setVisibility(View.GONE);
                });
    */

    private void fetchRepos() {
        loading.setValue(true);
        disposable.add(serviceMovies.getUpcomingMovies().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpcomingMoviesResponse>() {
                    @Override
                    public void onSuccess(UpcomingMoviesResponse value) {
                        repoLoadError.setValue(false);
                        movies.setValue(value.results);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        repoLoadError.setValue(true);
                        loading.setValue(false);
                    }
                }));
    }

    private void fetchReposNext(Long page) {
        loading.setValue(true);
        disposable.add(serviceMovies.getUpcomingMoviesNext(page).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpcomingMoviesResponse>() {
                    @Override
                    public void onSuccess(UpcomingMoviesResponse value) {
                        repoLoadError.setValue(false);
                        movies.setValue(value.results);
                        loading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        repoLoadError.setValue(true);
                        loading.setValue(false);
                    }
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null) {
            disposable.clear();
            disposable = null;
        }
    }
}

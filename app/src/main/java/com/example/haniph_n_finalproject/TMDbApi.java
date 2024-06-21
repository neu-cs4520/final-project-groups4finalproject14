package com.example.haniph_n_finalproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDbApi {
    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/now_playing")
    Call<MoviesResponse> getNowPlayingMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/upcoming")
    Call<MoviesResponse> getUpcomingMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/{movie_id}/videos")
    Call<TrailersResponse> getMovieTrailers(@Path("movie_id") String movieId, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetails(@Path("movie_id") String movieId, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}/credits")
    Call<CastResponse> getMovieCast(@Path("movie_id") String movieId, @Query("api_key") String apiKey);

    @GET("movie/{movie_id}/recommendations")
    Call<MoviesResponse> getRecommendedMovies(@Path("movie_id") String movieId, @Query("api_key") String apiKey);
}

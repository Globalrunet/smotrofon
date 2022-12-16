package com.mobilicos.smotrofon.network.services

import com.mobilicos.smotrofon.model.TrendingMovieResponse
import com.mobilicos.smotrofon.model.MovieDesc
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API Service
 */
interface MovieService {

    @GET("/3/trending/movie/week")
    suspend fun getPopularMovies() : Response<TrendingMovieResponse>

    @GET("/3/movie/{movie_id}")
    suspend fun getMovie(@Path("movie_id") id: Int) : Response<MovieDesc>
}
package com.ojsb.leanbacktest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class MovieList {

    private static HashMap<String, List<Movie>> sMovieList;
    private static HashMap<String, Movie> sMovieListById;

    public static Movie getMovieById(String mediaId) {
        return sMovieListById.get(mediaId);
    }

    public static HashMap<String, List<Movie>> getsMovieList() {
        return sMovieList;
    }

    public static HashMap<String, List<Movie>> buildMedia(int nCategories) {
        if (null != sMovieList) {
            return sMovieList;
        }
        sMovieList = new HashMap<>();
        sMovieListById = new HashMap<>();

        String title = new String();
        String studio = new String();

        for (int i = 0; i < nCategories; i++) {
            String category_name = String.format("Category %d", i);
            List<Movie> categoryList = new ArrayList<>();
            for (int j = 0; j < 20; j++) {
                String description = "This is description of a movie";
                title = String.format("Video %d-%d", i, j);
                studio = String.format("Studio %d", (i+j) % 7);
                Movie movie = buildMovieInfo(category_name, title, description, studio);
                sMovieListById.put(String.valueOf(movie.getId()), movie);
                categoryList.add(movie);
            }
            sMovieList.put(category_name, categoryList);
        }
        return sMovieList;
    }


    private static Movie buildMovieInfo(String category, String title,
                                        String description, String studio) {
        Movie movie = new Movie();
        movie.setId(Movie.getCount());
        Movie.incCount();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setStudio(studio);
        movie.setCategory(category);

        return movie;
    }
}

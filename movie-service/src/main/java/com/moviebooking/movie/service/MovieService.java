
package com.moviebooking.movie.service;

import com.moviebooking.movie.entity.Movie;
import com.moviebooking.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Movie not found with ID: " + id));
    }

    public List<Movie> searchMoviesByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Movie> getMoviesByGenre(String genre) {
        return movieRepository.findByGenreIgnoreCase(genre);
    }

    public List<Movie> getMoviesByLanguage(String language) {
        return movieRepository.findByLanguageIgnoreCase(language);
    }

    public Movie updateMovie(Long id, Movie updatedMovie) {
        Movie existingMovie = getMovieById(id);

        existingMovie.setTitle(updatedMovie.getTitle());
        existingMovie.setDescription(updatedMovie.getDescription());
        existingMovie.setLanguage(updatedMovie.getLanguage());
        existingMovie.setGenre(updatedMovie.getGenre());
        existingMovie.setDurationMinutes(
                updatedMovie.getDurationMinutes());

        return movieRepository.save(existingMovie);
    }

    public void deleteMovie(Long id) {
        Movie existingMovie = getMovieById(id);
        movieRepository.delete(existingMovie);
    }
}

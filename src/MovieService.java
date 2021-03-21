import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Scanner;

import util.DbConnection;

public class MovieService {

	static Connection con = DbConnection.getConnection();

	List<Movies> populateMovies(File file) throws FileNotFoundException {
		Scanner sc = new Scanner(file);
		List<Movies> list = new ArrayList<Movies>();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yy");
		while (sc.hasNextLine()) {
			String arr[] = sc.nextLine().split(",");
			Movies movie = new Movies(Integer.parseInt(arr[0]), arr[1], Category.valueOf(arr[2].replaceAll(" ", "_").toUpperCase()),
					Language.valueOf(arr[3].toUpperCase()), Date.valueOf(LocalDate.parse(arr[4], dateTimeFormatter)),
					Arrays.asList(arr[5].split("-")), Double.parseDouble(arr[6]), Double.parseDouble(arr[7]));
			list.add(movie);
		}
		return list;
	}

	public void addMovieInDb(Movies m) {
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement("insert into movie_tbl values (?,?,?,?,?,?,?)");
			pstmt.setInt(1, m.getMovieId());
			pstmt.setString(2, m.getMovieName());
			pstmt.setString(3, m.getMovieType().name());
			pstmt.setString(4, m.getLanguage().name());
			pstmt.setDate(5, m.getReleaseDate());
			pstmt.setDouble(6, m.getRating());
			pstmt.setDouble(7, m.getTotalBusinessDone());
			pstmt.executeUpdate();
			for (String s : m.getCasting()) {
				pstmt = con.prepareStatement("insert into casting_tbl values (?,?)");
				pstmt.setInt(1, m.getMovieId());
				pstmt.setString(2, s);
				pstmt.executeUpdate();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean storeAllMoviesInDb(List<Movies> movies) throws SQLException {
		for (Movies m : movies) {
			addMovieInDb(m);
		}
		return true;
	}

	public void addMovie(Movies movie, List<Movies> movies) {
		movies.add(movie);
		addMovieInDb(movie);
	}

	public void serializeMovies(List<Movies> movies, String fileName) {
		File file = new File(fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(movies);
			oos.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Movies> deserializeMovie(String filename) {
		List<Movies> movies = new ArrayList<>();
		File file = new File(filename);
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			movies = (List<Movies>) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return movies;
	}

	public List<Movies> getMoviesRealeasedInYear(List<Movies> movies, int year) {
		List<Movies> moviesReleasedInYear = new ArrayList<>();
		for (Movies m : movies) {
			LocalDate date = m.getReleaseDate().toLocalDate();
			if (date.getYear() == year)
				moviesReleasedInYear.add(m);
		}
		return moviesReleasedInYear;
	}

	public List<Movies> getMoviesByActor(List<Movies> movies, String... actorNames) {
		List<Movies> moviesByActor = new ArrayList<>();
		for (Movies m : movies) {
			for (String actor : actorNames) {
				if (m.getCasting().contains(actor)) {
					moviesByActor.add(m);
					break;
				}
			}
		}
		return moviesByActor;
	}

	public void updateRatings(Movies movie, double rating, List<Movies> movies) {
		if (movies.contains(movie)) {
			movie.setRating(rating);
			PreparedStatement pstmt = null;
			try {
				pstmt = con.prepareStatement("update movie_tbl set rating = ? where movieId = ?");
				pstmt.setDouble(1, rating);
				pstmt.setInt(2, movie.getMovieId());
				pstmt.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Rating Updated");
		} else {
			System.out.println("Movie does not exist in the list.");
		}
	}

	public void updateBusiness(Movies movie, double amount, List<Movies> movies) {
		if (movies.contains(movie)) {
			movie.setTotalBusinessDone(amount);
			PreparedStatement pstmt = null;
			try {
				pstmt = con.prepareStatement("update movie_tbl set totalBussinessDone = ? where movieId = ?");
				pstmt.setDouble(1, amount);
				pstmt.setInt(2, movie.getMovieId());
				pstmt.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Business Updated");
		} else {
			System.out.println("Movie does not exist in the list.");
		}
	}

	public Map<Language, Set<Movies>> businessDone(List<Movies> movies, double amount) {
		Set<Movies> movieSet = new TreeSet<>();
		Map<Language, Set<Movies>> movieMap = new HashMap<>();
		for (Movies movie : movies) {
			if (movie.getTotalBusinessDone() > amount) {
				movieSet.add(movie);
				if (movieMap.containsKey(movie.getLanguage())) {
					movieMap.get(movie.getLanguage()).add(movie);
				} else {
					movieMap.put(movie.getLanguage(), movieSet);
				}
			}
		}
		return movieMap;
	}
}

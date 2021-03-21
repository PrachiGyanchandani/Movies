import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public class MovieTester {
	public static void main(String[] args) throws FileNotFoundException, SQLException {
		MovieService ms = new MovieService();
		File file = new File("F:\\Persistent Training\\Java\\Ass09_Prachi_Gyanchandani\\MovieDetails.txt");
		List<Movies> list = ms.populateMovies(file);
		//ms.storeAllMoviesInDb(list);
		ms.serializeMovies(list, "testfile");
		ms.deserializeMovie("testfile");
		System.out.println(ms.getMoviesRealeasedInYear(list, 2012));
		System.out.println(ms.getMoviesByActor(list, "Alia Bhatt"));
		ms.updateRatings(list.get(0), 9, list);
		ms.updateBusiness(list.get(0), 9900, list);
		System.out.println(ms.businessDone(list, 400));
	}
}

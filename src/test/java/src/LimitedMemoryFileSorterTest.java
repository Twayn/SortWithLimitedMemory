package src;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class LimitedMemoryFileSorterTest {
	private List<String> unsorted = List.of("sad", "fgd", "sdf", "sgfdg", "sdf", "as", "wqe", "r", "ert", "uyi", "yui", "uy", "v", "pov", "pou", "vs", "av", "fva", "v", "ghf", "jkh", "jk", "po", "poi", "u", "ui", "er", "rt", "iou", "l", "k", "m", "u", "jh", "vc", "sdf", "wq", "ee", "r", "rt", "rt", "ui", "io", "h", "g", "s", "ds", "fsd", "as", "vc", "zx", "xc", "ds", "vc", "gf", "hg", "jh", "ad", "das", "fds", "asd", "das", "gf", "asd", "fsd", "tr", "we", "da", "sda", "zx");

	private final String unsortedFileName = "unsorted.txt";
	private final String sortedFileName = "sorted.txt";

	@Test
	void sortingShoulBeDone() throws IOException {
		Files.write(Paths.get(unsortedFileName), unsorted);

		LimitedMemoryFileSorter sorter = new LimitedMemoryFileSorter();
		sorter.sort(unsortedFileName, sortedFileName, 10);

		List<String> sortedActual = Files.readAllLines(Paths.get(sortedFileName));

		List<String> sortedExpected = new ArrayList<>(unsorted);
		sortedExpected.sort(Collections.reverseOrder());

		assertEquals(sortedExpected, sortedActual);
	}
}
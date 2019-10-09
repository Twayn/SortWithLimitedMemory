package src;

import static java.util.Collections.max;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.SneakyThrows;

public class LimitedMemoryFileSorter {
	private static String unsortedFile = "in.txt";
	private static String sortedFile = "out.txt";
	private static int chunksNum = 50;

	public static void main(String[] args) throws IOException {
		LimitedMemoryFileSorter sorter = new LimitedMemoryFileSorter();
		sorter.sort(unsortedFile, sortedFile, chunksNum);
	}

	public void sort(String fileName, String resFileName, int chunksNum) throws IOException {
		List<Chunk> chunks = splitToChunks(fileName, chunksNum);
		chunks.forEach(Chunk::sort);
		chunks.forEach(Chunk::openForRead);
		merge(chunks, resFileName);
	}

	private void merge(List<Chunk> chunks, String resFileName) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(resFileName))) {
			for (; ; ) {
				chunks.removeIf((chunk) -> chunk.getLine() == null);
				if (chunks.size() == 0) break;

				List<String> strings = chunks.stream().map(Chunk::getLine).collect(toList());

				int indexOfMax = strings.indexOf(max(strings));
				writer.write(chunks.get(indexOfMax).getLine() + System.lineSeparator());
				chunks.get(indexOfMax).next();
			}
		}
	}

	private List<Chunk> splitToChunks(String fileName, int chunksNum) throws IOException {
		List<Chunk> chunks = range(0, chunksNum).mapToObj(Chunk::new).collect(toList());
		
		AtomicInteger i = new AtomicInteger(-1);
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			stream.forEach((line) -> {
				chunks.get(i.incrementAndGet() % chunksNum).addLine(line);
			});
		}

		chunks.forEach(Chunk::closeWrite);

		return chunks;
	}

	static class Chunk {
		private final String fileName;
		private final BufferedWriter writer;

		private BufferedReader reader;
		private String line;

		@SneakyThrows(IOException.class)
		Chunk(int num) {
			this.fileName = num + ".txt";
			this.writer = new BufferedWriter(new FileWriter(fileName));
		}

		@SneakyThrows(IOException.class)
		void addLine(String line) {
			writer.write(line + System.lineSeparator());
		}

		@SneakyThrows(IOException.class)
		void closeWrite() {
			writer.close();
		}

		@SneakyThrows(IOException.class)
		void sort() {
			List<String> content = Files.readAllLines(Paths.get(fileName));
			content.sort(Collections.reverseOrder());
			Files.write(Paths.get(fileName), content);
		}

		@SneakyThrows(IOException.class)
		void openForRead() {
			reader = new BufferedReader(new FileReader(fileName));
			next();
		}

		@SneakyThrows(IOException.class)
		void next() {
			line = reader.readLine();
		}

		String getLine() {
			if (line == null) delete();
			return line;
		}

		@SneakyThrows(IOException.class)
		private void delete() {
			reader.close();
			new File(fileName).delete();
		}
	}
}

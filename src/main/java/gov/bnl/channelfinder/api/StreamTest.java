package gov.bnl.channelfinder.api;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class StreamTest {

	public static void main(String[] args) {
		List<String> numbers = Arrays.asList("1", "2", "3", "50");
//		numbers.stream().max((s) -> {} );
		int maximum;
		Optional<String> max = numbers.stream().max((o1, o2) -> {
				return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
		});
		System.out.println(max.get());
		if (max.isPresent()) {
			maximum = Integer.valueOf(max.get());
		}
	}
	
}

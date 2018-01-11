package protochat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;

public class ConsoleMenu {

	private static final char SEP_CHAR = '=';
	private static final int SEP_MARGIN = 3;
	private static final String PROMPT = " > ";

	public static final int EXIT = 0;
	public static final int INVALID_OPTION = -1;
	public static final int EOF = -2;

	private String title;
	private String separator;
	private List<String> options;
	private boolean hasExitOption;
	private int op;

	private final Scanner input;

	public ConsoleMenu() { this("", null, true); }

	public ConsoleMenu(String title, String[] options, boolean hasExitOption) {
		this.title = (title == null) ? "" : title;
		this.options = new ArrayList<>(Arrays.asList(options));
		this.hasExitOption = hasExitOption;
		this.input = new Scanner(System.in);
		this.op = INVALID_OPTION; // no valid option yet
		this.separator = ConsoleMenu.generateSeparatorFor(title, this.options, hasExitOption);
	}

	private static String generateSeparatorFor(String title, List<String> options, boolean hasExitOption) {
		int maxLen = title.length();

		if (maxLen < 4 && hasExitOption) // "Exit" (with length 4) may be larger than the title
			maxLen = 4;

		for (String op : options) {
			int len = op.length();

			if (len > maxLen)
				maxLen = len;
		}
		return new String(new char[title.length() + SEP_MARGIN]).replace('\0', SEP_CHAR);
	}

	public void setTitle(String title) {
		this.title = (title == null) ? "" : title;
		if (title.length() + SEP_MARGIN > separator.length()) // separator.length() includes SEP_MARGIN
			separator = new String(new char[title.length() + SEP_MARGIN]).replace('\0', SEP_CHAR);
	}

	public void add(String op) {
		options.add(op);

		if (op.length() + SEP_MARGIN > separator.length())
			separator = new String(new char[op.length() + SEP_MARGIN]).replace('\0', SEP_CHAR);
	}

	public String remove(int i) throws IndexOutOfBoundsException {
		String op = options.remove(i);

		if (op.length() + SEP_MARGIN == separator.length()) {
			separator = generateSeparatorFor(title, options, hasExitOption);
		}
		return op;
	}

	public boolean remove(String op) {
		boolean res = options.remove(op);

		if (res && op.length() + SEP_MARGIN == separator.length()) {
			separator = generateSeparatorFor(title, options, hasExitOption);
		}
		return res;
	}

	public void run() {
		do {
			show();
			op = readOption();
		} while (op == INVALID_OPTION);
	}

	public void show() {
		System.out.println(separator);
		System.out.println(title);
		System.out.println(separator);
		for (int i = 1; i <= options.size(); i++) {
			System.out.format("%2d. %s%n", i, options.get(i-1));
		}
		if (hasExitOption)
			System.out.println(" 0. Exit");

		System.out.println(separator);
	}

	public int readOption() {
		int op;

		System.out.print(PROMPT);
		try {
			op = input.nextInt();
			if (op > options.size() || (hasExitOption && op < 0) || (!hasExitOption && op < 1)) {
				op = INVALID_OPTION;
				System.err.println("Invalid option.");
				input.nextLine();
			}
		} catch (InputMismatchException e) {
			op = INVALID_OPTION;
			System.err.println("Invalid option.");
			input.nextLine();
		} catch (NoSuchElementException e) { // EOF
			op = EOF;
		}
		return op;
	}

	public int getOption() {
		return op;
	}

	public void clear() {
		title = separator = "";
		options.clear();
		hasExitOption = false;
		op = -1;
	}
}

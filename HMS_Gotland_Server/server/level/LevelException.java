package level;

public class LevelException extends Exception {
	private static final long serialVersionUID = -1517108261787652522L;

	public String message;
	public String level;
	public int line = 0;

	public LevelException(String level, String s, int i) {
		message = s;
		this.level = level;
		line = i;
	}

	@Override
	public String getMessage() {
		return level + " - " + message + " @line " + line;
	}
}
